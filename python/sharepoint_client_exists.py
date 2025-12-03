import msal
import requests
import json
import os
from pathlib import Path
from typing import Optional, Dict, List, Any

SharePointClient:

def _item_exists(self, item_path: str) -> Tuple[bool, Optional[str], Optional[Dict]]:
    """
    检查指定路径的项目是否存在，并返回详细信息
    
    Args:
        item_path: 项目在驱动器中的完整路径
    
    Returns:
        tuple: (是否存在, 项目ID（如果存在）, 项目完整信息（如果存在）)
    """
    try:
        endpoint = f"/drives/{self.drive_id}/root:{item_path}"
        response = self._make_request("GET", endpoint)
        
        if response and response.status_code == 200:
            item_data = response.json()
            return True, item_data.get("id"), item_data
        else:
            # 项目不存在或其他错误
            return False, None, None
            
    except Exception as e:
        # 对于网络异常等，我们视为不存在，具体由调用者处理
        print(f"⚠️  检查项目存在性时发生异常（视为不存在）: {str(e)}")
        return False, None, None
        
def upload_file(self, local_path: str, remote_folder: str, 
               remote_name: Optional[str] = None,
               conflict_behavior: str = "rename") -> Optional[Dict[str, Any]]:
    """
    上传文件（增强版：包含存在性检查和冲突处理）
    
    Args:
        local_path: 本地文件路径
        remote_folder: 远程文件夹路径
        remote_name: 远程文件名（可选）
        conflict_behavior: 冲突处理方式
            - "fail": 如果存在则失败（默认）
            - "replace": 如果存在则替换
            - "rename": 如果存在则自动重命名（默认，添加后缀）
    
    Returns:
        上传的文件信息或None
    """
    # ... [参数验证和驱动器检查代码保持不变] ...

    # 构建远程路径
    remote_path = f"{remote_folder.rstrip('/')}/{remote_name}"
    
    # 1. 检查目标文件是否存在
    exists, item_id, existing_item = self._item_exists(remote_path)
    
    final_remote_path = remote_path
    if exists:
        print(f"⚠️  目标路径已存在文件: {remote_path}")
        
        if conflict_behavior == "fail":
            print("❌ 冲突处理策略为 'fail'，上传中止。")
            return None
        elif conflict_behavior == "replace":
            print("🔄 冲突处理策略为 'replace'，将替换原有文件。")
            # 在PUT上传中，直接上传即可替换，无需额外操作
            final_remote_path = remote_path
        elif conflict_behavior == "rename":
            # 生成一个带时间戳的新文件名
            name_parts = os.path.splitext(remote_name)
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            new_name = f"{name_parts[0]}_{timestamp}{name_parts[1]}"
            final_remote_path = f"{remote_folder.rstrip('/')}/{new_name}"
            print(f"🔄 冲突处理策略为 'rename'，新路径: {final_remote_path}")
            
            # 递归检查新名称是否也存在（理论上极小概率，但安全起见）
            if self._item_exists(final_remote_path)[0]:
                # 如果连时间戳都冲突，追加随机数
                import random
                new_name = f"{name_parts[0]}_{timestamp}_{random.randint(1000,9999)}{name_parts[1]}"
                final_remote_path = f"{remote_folder.rstrip('/')}/{new_name}"
                print(f"🔄 重命名后仍冲突，使用最终路径: {final_remote_path}")
        else:
            print(f"❌ 未知的冲突处理策略: {conflict_behavior}，上传中止。")
            return None
    
    # 2. 执行上传（使用最终确定的路径 final_remote_path）
    # ... [原有的上传逻辑] ...
    upload_url = f"/drives/{self.drive_id}/root:{final_remote_path}:/content"
    print(f"📤 正在上传至: {final_remote_path}")
    # ... [继续执行上传] ...
    
    
def create_folder(self, parent_path: str, folder_name: str,
                 conflict_behavior: str = "rename") -> Optional[Dict[str, Any]]:
    """
    创建文件夹（增强版：包含存在性检查和冲突处理）
    
    Args:
        parent_path: 父文件夹路径
        folder_name: 新文件夹名称
        conflict_behavior: 冲突处理方式
            - "fail": 如果存在则失败
            - "rename": 如果存在则自动重命名（默认）
    
    Returns:
        创建的文件夹信息或None
    """
    # ... [参数验证和驱动器检查] ...
    
    target_path = f"{parent_path.rstrip('/')}/{folder_name}"
    
    # 检查是否已存在
    exists, _, _ = self._item_exists(target_path)
    
    final_folder_name = folder_name
    if exists:
        print(f"⚠️  目标文件夹已存在: {target_path}")
        
        if conflict_behavior == "fail":
            print("❌ 冲突处理策略为 'fail'，创建中止。")
            return None
        elif conflict_behavior == "rename":
            # 添加数字后缀进行重命名
            counter = 1
            while exists and counter < 100: # 设置一个上限
                new_folder_name = f"{folder_name}_{counter}"
                target_path = f"{parent_path.rstrip('/')}/{new_folder_name}"
                exists, _, _ = self._item_exists(target_path)
                if not exists:
                    final_folder_name = new_folder_name
                    break
                counter += 1
            if exists:
                print("❌ 无法生成不重复的文件夹名，创建中止。")
                return None
            print(f"🔄 冲突处理策略为 'rename'，新文件夹名: {final_folder_name}")
    
    # 使用最终名称创建文件夹
    create_url = f"/drives/{self.drive_id}/root:{parent_path}:/children"
    folder_data = {
        "name": final_folder_name,
        "folder": {},
        "@microsoft.graph.conflictBehavior": "rename" # API层面的兜底策略
    }
    # ... [执行创建请求] ...
    
    
def move_item(self, source_path: str, target_folder: str, 
             new_name: Optional[str] = None,
             conflict_behavior: str = "rename") -> Optional[Dict[str, Any]]:
    """
    移动或重命名项目（增强版：包含目标存在性检查）
    
    Args:
        source_path: 源项目路径
        target_folder: 目标文件夹路径
        new_name: 新名称（可选）
        conflict_behavior: 当目标位置存在同名项目时的处理方式
            - "fail": 如果存在则失败
            - "replace": 如果存在则替换（需谨慎，可能导致数据丢失）
            - "rename": 如果存在则自动重命名源文件（默认）
    
    Returns:
        移动后的项目信息或None
    """
    # ... [获取源项目和目标文件夹ID的代码] ...
    
    # 确定目标路径下的最终名称
    target_name = new_name if new_name else os.path.basename(source_path)
    potential_target_path = f"{target_folder.rstrip('/')}/{target_name}"
    
    # 检查目标位置是否已存在同名项目
    exists, existing_id, _ = self._item_exists(potential_target_path)
    
    final_target_name = target_name
    if exists:
        print(f"⚠️  目标位置已存在同名项目: {potential_target_path}")
        
        if conflict_behavior == "fail":
            print("❌ 冲突处理策略为 'fail'，移动中止。")
            return None
        elif conflict_behavior == "replace":
            print("⚠️  冲突处理策略为 'replace'，将替换目标文件。")
            # 注意：此操作不可逆，生产环境慎用
            final_target_name = target_name
        elif conflict_behavior == "rename":
            # 为源文件重命名
            name_parts = os.path.splitext(target_name)
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            final_target_name = f"{name_parts[0]}_{timestamp}{name_parts[1]}"
            print(f"🔄 冲突处理策略为 'rename'，移动后名称改为: {final_target_name}")
    
    # 准备更新数据（使用最终确定的名称）
    update_data = {
        "parentReference": {
            "id": target_id,
            "driveId": self.drive_id
        },
        "name": final_target_name
    }
    # ... [执行移动请求] ...