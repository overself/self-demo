import msal
import requests
import json
import os
from pathlib import Path
from typing import Optional, Dict, List, Any

class SharePointGraphAPI:
    """
    通过Microsoft Graph API操作SharePoint文件的完整工具类
    """
    
    def __init__(self, tenant_id: str, client_id: str, client_secret: str, 
                 site_hostname: str, site_path: str):
        """
        初始化SharePoint Graph API客户端
        
        Args:
            tenant_id: Azure租户ID
            client_id: 应用客户端ID
            client_secret: 客户端密钥
            site_hostname: SharePoint主机名 (如: your-domain.sharepoint.com)
            site_path: 站点路径 (如: sites/YourSiteName)
        """
        self.tenant_id = tenant_id
        self.client_id = client_id
        self.client_secret = client_secret
        self.site_hostname = site_hostname
        self.site_path = site_path
        
        # API端点
        self.graph_endpoint = "https://graph.microsoft.com/v1.0"
        
        # 认证信息
        self.access_token = None
        self.headers = None
        
        # 缓存站点和驱动器ID
        self.site_id = None
        self.drive_id = None
        
        # 初始化认证
        self._authenticate()
        
    def _authenticate(self) -> bool:
        """
        获取访问令牌
        """
        authority = f"https://login.microsoftonline.com/{self.tenant_id}"
        scope = ["https://graph.microsoft.com/.default"]
        
        try:
            # 创建MSAL应用
            app = msal.ConfidentialClientApplication(
                client_id=self.client_id,
                client_credential=self.client_secret,
                authority=authority
            )
            
            # 获取令牌
            result = app.acquire_token_for_client(scopes=scope)
            
            if "access_token" in result:
                self.access_token = result["access_token"]
                self.headers = {
                    "Authorization": f"Bearer {self.access_token}",
                    "Content-Type": "application/json"
                }
                print("✅ 认证成功！令牌已获取")
                return True
            else:
                error_msg = result.get("error_description", "未知错误")
                print(f"❌ 认证失败: {error_msg}")
                return False
                
        except Exception as e:
            print(f"❌ 认证过程中发生异常: {str(e)}")
            return False
    
    def _get_site_and_drive(self) -> bool:
        """
        获取站点ID和驱动器ID
        """
        try:
            # 获取站点ID
            get_site_url = f"{self.graph_endpoint}/sites/{self.site_hostname}:/{self.site_path}"
            response = requests.get(get_site_url, headers=self.headers)
            response.raise_for_status()
            
            site_data = response.json()
            self.site_id = site_data["id"]
            print(f"✅ 站点ID获取成功: {self.site_id}")
            
            # 获取驱动器ID（默认文档库）
            get_drives_url = f"{self.graph_endpoint}/sites/{self.site_id}/drives"
            response = requests.get(get_drives_url, headers=self.headers)
            response.raise_for_status()
            
            drives_data = response.json()
            if drives_data["value"]:
                self.drive_id = drives_data["value"][0]["id"]
                drive_name = drives_data["value"][0]["name"]
                print(f"✅ 驱动器获取成功 - ID: {self.drive_id}, 名称: {drive_name}")
                return True
            else:
                print("❌ 未找到可用的驱动器")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ 获取站点/驱动器时出错: {str(e)}")
            return False
    
    def get_item_id_by_path(self, item_path: str) -> Optional[str]:
        """
        通过路径获取文件或文件夹的ID
        
        Args:
            item_path: 相对于驱动器根目录的路径，如 "/Shared Documents/MyFolder/file.txt"
            
        Returns:
            项目ID或None
        """
        try:
            url = f"{self.graph_endpoint}/drives/{self.drive_id}/root:{item_path}"
            response = requests.get(url, headers=self.headers)
            
            if response.status_code == 200:
                item_data = response.json()
                return item_data["id"]
            else:
                print(f"⚠️ 未找到路径: {item_path}")
                return None
                
        except Exception as e:
            print(f"❌ 获取项目ID时出错: {str(e)}")
            return None
    
    def list_directory(self, folder_path: str = "/") -> List[Dict[str, Any]]:
        """
        列出目录内容
        
        Args:
            folder_path: 文件夹路径，默认为根目录
            
        Returns:
            目录项列表
        """
        try:
            url = f"{self.graph_endpoint}/drives/{self.drive_id}/root:{folder_path}:/children"
            response = requests.get(url, headers=self.headers)
            response.raise_for_status()
            
            items = response.json()["value"]
            result = []
            
            for item in items:
                item_info = {
                    "name": item.get("name", ""),
                    "id": item.get("id", ""),
                    "type": "folder" if "folder" in item else "file",
                    "size": item.get("size", 0),
                    "lastModified": item.get("lastModifiedDateTime", ""),
                    "webUrl": item.get("webUrl", "")
                }
                result.append(item_info)
                
            return result
            
        except Exception as e:
            print(f"❌ 列出目录时出错: {str(e)}")
            return []
    
    def upload_file(self, local_file_path: str, remote_folder_path: str, 
                   remote_file_name: Optional[str] = None) -> bool:
        """
        上传文件到SharePoint
        
        Args:
            local_file_path: 本地文件路径
            remote_folder_path: 远程文件夹路径
            remote_file_name: 远程文件名（可选，默认使用本地文件名）
            
        Returns:
            上传是否成功
        """
        try:
            # 检查本地文件是否存在
            if not os.path.exists(local_file_path):
                print(f"❌ 本地文件不存在: {local_file_path}")
                return False
            
            # 设置远程文件名
            if remote_file_name is None:
                remote_file_name = os.path.basename(local_file_path)
            
            # 构建远程路径
            remote_path = f"{remote_folder_path.rstrip('/')}/{remote_file_name}"
            
            # 上传URL
            upload_url = f"{self.graph_endpoint}/drives/{self.drive_id}/root:{remote_path}:/content"
            
            # 读取文件内容
            with open(local_file_path, 'rb') as file:
                file_content = file.read()
            
            # 上传文件
            upload_headers = self.headers.copy()
            upload_headers["Content-Type"] = "application/octet-stream"
            
            response = requests.put(upload_url, headers=upload_headers, data=file_content)
            
            if response.status_code in [200, 201]:
                print(f"✅ 文件上传成功: {remote_path}")
                return True
            else:
                print(f"❌ 文件上传失败: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            print(f"❌ 上传文件时出错: {str(e)}")
            return False
    
    def download_file(self, remote_file_path: str, local_save_path: str) -> bool:
        """
        从SharePoint下载文件
        
        Args:
            remote_file_path: 远程文件路径
            local_save_path: 本地保存路径
            
        Returns:
            下载是否成功
        """
        try:
            # 确保本地目录存在
            local_dir = os.path.dirname(local_save_path)
            if local_dir and not os.path.exists(local_dir):
                os.makedirs(local_dir, exist_ok=True)
            
            # 下载URL
            download_url = f"{self.graph_endpoint}/drives/{self.drive_id}/root:{remote_file_path}:/content"
            
            response = requests.get(download_url, headers=self.headers)
            
            if response.status_code == 200:
                with open(local_save_path, 'wb') as file:
                    file.write(response.content)
                print(f"✅ 文件下载成功: {local_save_path}")
                return True
            else:
                print(f"❌ 文件下载失败: {response.status_code}")
                return False
                
        except Exception as e:
            print(f"❌ 下载文件时出错: {str(e)}")
            return False
    
    def move_file(self, source_path: str, target_folder_path: str, 
                 new_name: Optional[str] = None) -> bool:
        """
        移动或重命名文件
        
        Args:
            source_path: 源文件路径
            target_folder_path: 目标文件夹路径
            new_name: 新文件名（可选，默认保持原名）
            
        Returns:
            移动是否成功
        """
        try:
            # 获取源文件ID
            source_item_id = self.get_item_id_by_path(source_path)
            if not source_item_id:
                print(f"❌ 未找到源文件: {source_path}")
                return False
            
            # 获取目标文件夹ID
            target_folder_id = self.get_item_id_by_path(target_folder_path)
            if not target_folder_id:
                print(f"❌ 未找到目标文件夹: {target_folder_path}")
                return False
            
            # 移动URL
            move_url = f"{self.graph_endpoint}/drives/{self.drive_id}/items/{source_item_id}"
            
            # 构建请求体
            move_body = {
                "parentReference": {
                    "id": target_folder_id,
                    "driveId": self.drive_id
                }
            }
            
            # 如果指定了新名称，则重命名
            if new_name:
                move_body["name"] = new_name
            
            response = requests.patch(move_url, headers=self.headers, json=move_body)
            
            if response.status_code == 200:
                print(f"✅ 文件移动成功: {source_path} -> {target_folder_path}")
                return True
            else:
                print(f"❌ 文件移动失败: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            print(f"❌ 移动文件时出错: {str(e)}")
            return False
    
    def copy_file(self, source_path: str, target_path: str) -> bool:
        """
        复制文件
        
        Args:
            source_path: 源文件路径
            target_path: 目标文件路径
            
        Returns:
            复制是否成功
        """
        try:
            # 获取源文件ID
            source_item_id = self.get_item_id_by_path(source_path)
            if not source_item_id:
                print(f"❌ 未找到源文件: {source_path}")
                return False
            
            # 复制URL
            copy_url = f"{self.graph_endpoint}/drives/{self.drive_id}/items/{source_item_id}/copy"
            
            # 构建请求体
            copy_body = {
                "parentReference": {
                    "driveId": self.drive_id,
                    "path": f"/drive/root:{os.path.dirname(target_path)}"
                },
                "name": os.path.basename(target_path)
            }
            
            response = requests.post(copy_url, headers=self.headers, json=copy_body)
            
            if response.status_code in [200, 202]:
                print(f"✅ 文件复制请求已接受: {source_path} -> {target_path}")
                
                # 复制操作是异步的，这里可以添加代码来检查复制状态
                # 通过response.headers["Location"]获取状态URL
                return True
            else:
                print(f"❌ 文件复制失败: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            print(f"❌ 复制文件时出错: {str(e)}")
            return False
    
    def delete_item(self, item_path: str) -> bool:
        """
        删除文件或文件夹
        
        Args:
            item_path: 项目路径
            
        Returns:
            删除是否成功
        """
        try:
            # 获取项目ID
            item_id = self.get_item_id_by_path(item_path)
            if not item_id:
                print(f"❌ 未找到项目: {item_path}")
                return False
            
            # 删除URL
            delete_url = f"{self.graph_endpoint}/drives/{self.drive_id}/items/{item_id}"
            
            response = requests.delete(delete_url, headers=self.headers)
            
            if response.status_code == 204:
                print(f"✅ 项目删除成功: {item_path}")
                return True
            else:
                print(f"❌ 项目删除失败: {response.status_code}")
                return False
                
        except Exception as e:
            print(f"❌ 删除项目时出错: {str(e)}")
            return False
    
    def create_folder(self, parent_folder_path: str, folder_name: str) -> bool:
        """
        创建新文件夹
        
        Args:
            parent_folder_path: 父文件夹路径
            folder_name: 新文件夹名称
            
        Returns:
            创建是否成功
        """
        try:
            # 创建文件夹URL
            create_url = f"{self.graph_endpoint}/drives/{self.drive_id}/root:{parent_folder_path}:/children"
            
            # 构建请求体
            folder_body = {
                "name": folder_name,
                "folder": {},
                "@microsoft.graph.conflictBehavior": "rename"
            }
            
            response = requests.post(create_url, headers=self.headers, json=folder_body)
            
            if response.status_code == 201:
                print(f"✅ 文件夹创建成功: {folder_name}")
                return True
            else:
                print(f"❌ 文件夹创建失败: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            print(f"❌ 创建文件夹时出错: {str(e)}")
            return False
    
    def search_items(self, search_query: str) -> List[Dict[str, Any]]:
        """
        搜索文件或文件夹
        
        Args:
            search_query: 搜索关键词
            
        Returns:
            搜索结果列表
        """
        try:
            # 搜索URL
            search_url = f"{self.graph_endpoint}/drives/{self.drive_id}/root/search(q='{search_query}')"
            
            response = requests.get(search_url, headers=self.headers)
            response.raise_for_status()
            
            items = response.json()["value"]
            result = []
            
            for item in items:
                item_info = {
                    "name": item.get("name", ""),
                    "path": item.get("parentReference", {}).get("path", ""),
                    "type": "folder" if "folder" in item else "file",
                    "size": item.get("size", 0),
                    "lastModified": item.get("lastModifiedDateTime", ""),
                    "webUrl": item.get("webUrl", "")
                }
                result.append(item_info)
                
            return result
            
        except Exception as e:
            print(f"❌ 搜索时出错: {str(e)}")
            return []


# ============================================================================
# 使用示例
# ============================================================================

def main():
    """
    SharePoint Graph API 使用示例
    """
    print("=" * 60)
    print("SharePoint Graph API 示例")
    print("=" * 60)
    
    # ============================================================
    # 配置信息 - 请替换为您的实际信息
    # ============================================================
    CONFIG = {
        "tenant_id": "YOUR_TENANT_ID",                    # Azure租户ID
        "client_id": "YOUR_CLIENT_ID",                    # 应用客户端ID
        "client_secret": "YOUR_CLIENT_SECRET",            # 客户端密钥
        "site_hostname": "your-domain.sharepoint.com",    # SharePoint主机名
        "site_path": "sites/YourSiteName"                 # 站点路径
    }
    
    # 检查配置是否已填写
    if "YOUR_" in CONFIG["tenant_id"]:
        print("⚠️ 请先配置您的Azure应用信息！")
        print("\n配置步骤:")
        print("1. 访问 https://portal.azure.com")
        print("2. 进入 Azure Active Directory > 应用注册")
        print("3. 创建或选择应用，获取:")
        print("   - 租户ID (Tenant ID)")
        print("   - 客户端ID (Client ID)")
        print("   - 创建客户端密钥 (Client Secret)")
        print("4. 为应用添加 Microsoft Graph 权限:")
        print("   - Files.ReadWrite.All")
        print("   - Sites.ReadWrite.All")
        return
    
    # ============================================================
    # 初始化客户端
    # ============================================================
    print("\n1. 初始化SharePoint客户端...")
    sp_client = SharePointGraphAPI(
        tenant_id=CONFIG["tenant_id"],
        client_id=CONFIG["client_id"],
        client_secret=CONFIG["client_secret"],
        site_hostname=CONFIG["site_hostname"],
        site_path=CONFIG["site_path"]
    )
    
    # 获取站点和驱动器ID
    if not sp_client._get_site_and_drive():
        print("❌ 初始化失败，请检查配置和网络连接")
        return
    
    print("✅ SharePoint客户端初始化完成！")
    
    # ============================================================
    # 示例1: 列出根目录内容
    # ============================================================
    print("\n" + "=" * 60)
    print("示例1: 列出根目录内容")
    print("=" * 60)
    
    items = sp_client.list_directory("/")
    if items:
        print(f"找到 {len(items)} 个项目:")
        for i, item in enumerate(items, 1):
            item_type = "📁" if item["type"] == "folder" else "📄"
            print(f"{i}. {item_type} {item['name']} ({item['type']})")
    else:
        print("目录为空或访问失败")
    
    # ============================================================
    # 示例2: 创建测试文件夹
    # ============================================================
    print("\n" + "=" * 60)
    print("示例2: 创建测试文件夹")
    print("=" * 60)
    
    test_folder = "/Shared Documents/GraphAPI_Test"
    if sp_client.create_folder("/Shared Documents", "GraphAPI_Test"):
        print(f"✅ 测试文件夹创建成功: {test_folder}")
    else:
        print("⚠️ 文件夹可能已存在，继续执行示例...")
    
    # ============================================================
    # 示例3: 上传测试文件
    # ============================================================
    print("\n" + "=" * 60)
    print("示例3: 上传测试文件")
    print("=" * 60)
    
    # 创建本地测试文件
    test_content = """这是一个通过Graph API上传的测试文件。
创建时间: 2024年
用途: 演示Graph API文件操作功能。
"""
    
    local_test_file = "test_upload.txt"
    with open(local_test_file, "w", encoding="utf-8") as f:
        f.write(test_content)
    
    print(f"📝 创建本地测试文件: {local_test_file}")
    
    # 上传文件
    remote_path = f"{test_folder}/test_file.txt"
    if sp_client.upload_file(local_test_file, test_folder, "api_test_file.txt"):
        print(f"✅ 文件上传成功到: {test_folder}/api_test_file.txt")
    else:
        print("❌ 文件上传失败")
    
    # ============================================================
    # 示例4: 列出测试文件夹内容
    # ============================================================
    print("\n" + "=" * 60)
    print("示例4: 列出测试文件夹内容")
    print("=" * 60)
    
    test_items = sp_client.list_directory(test_folder)
    if test_items:
        print(f"测试文件夹内容 ({test_folder}):")
        for item in test_items:
            size_mb = item["size"] / (1024 * 1024) if item["size"] > 0 else 0
            print(f"  - {item['name']} ({item['type']}, {size_mb:.2f} MB)")
    else:
        print("测试文件夹为空")
    
    # ============================================================
    # 示例5: 下载文件
    # ============================================================
    print("\n" + "=" * 60)
    print("示例5: 下载文件")
    print("=" * 60)
    
    download_path = "downloaded_file.txt"
    if sp_client.download_file(f"{test_folder}/api_test_file.txt", download_path):
        # 读取下载的文件内容
        with open(download_path, "r", encoding="utf-8") as f:
            content = f.read()
        print(f"✅ 文件下载成功，内容预览: {content[:100]}...")
    else:
        print("❌ 文件下载失败")
    
    # ============================================================
    # 示例6: 复制文件
    # ============================================================
    print("\n" + "=" * 60)
    print("示例6: 复制文件")
    print("=" * 60)
    
    source_file = f"{test_folder}/api_test_file.txt"
    target_file = f"{test_folder}/api_test_file_copy.txt"
    
    if sp_client.copy_file(source_file, target_file):
        print(f"✅ 文件复制请求已发送: {source_file} -> {target_file}")
    else:
        print("❌ 文件复制失败")
    
    # ============================================================
    # 示例7: 移动/重命名文件
    # ============================================================
    print("\n" + "=" * 60)
    print("示例7: 移动/重命名文件")
    print("=" * 60)
    
    if sp_client.move_file(
        source_path=f"{test_folder}/api_test_file_copy.txt",
        target_folder_path=test_folder,
        new_name="renamed_file.txt"
    ):
        print(f"✅ 文件重命名成功")
    else:
        print("❌ 文件重命名失败")
    
    # ============================================================
    # 示例8: 搜索文件
    # ============================================================
    print("\n" + "=" * 60)
    print("示例8: 搜索文件")
    print("=" * 60)
    
    search_results = sp_client.search_items("test")
    if search_results:
        print(f"搜索 'test' 找到 {len(search_results)} 个结果:")
        for i, result in enumerate(search_results, 1):
            print(f"{i}. {result['name']} (路径: {result['path']})")
    else:
        print("未找到搜索结果")
    
    # ============================================================
    # 示例9: 删除测试文件
    # ============================================================
    print("\n" + "=" * 60)
    print("示例9: 清理测试文件")
    print("=" * 60)
    
    # 删除测试文件
    files_to_delete = [
        f"{test_folder}/api_test_file.txt",
        f"{test_folder}/renamed_file.txt"
    ]
    
    for file_path in files_to_delete:
        if sp_client.delete_item(file_path):
            print(f"✅ 已删除: {file_path}")
        else:
            print(f"⚠️ 删除失败或文件不存在: {file_path}")
    
    # ============================================================
    # 清理本地文件
    # ============================================================
    print("\n" + "=" * 60)
    print("清理本地文件")
    print("=" * 60)
    
    local_files = [local_test_file, download_path]
    for file_path in local_files:
        if os.path.exists(file_path):
            os.remove(file_path)
            print(f"🗑️  已删除本地文件: {file_path}")
    
    print("\n" + "=" * 60)
    print("示例执行完成！")
    print("=" * 60)


if __name__ == "__main__":
    main()