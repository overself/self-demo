import ssl
import certifi
from typing import Union, Optional

class SharePointClient:
    def __init__(self, config: Dict[str, str], ssl_verify: Union[bool, str] = True):
        """
        初始化SharePoint客户端
        
        Args:
            config: 配置字典
            ssl_verify: SSL验证配置
                - True: 使用系统默认证书（推荐生产环境）
                - False: 禁用SSL验证（仅用于测试，不安全！）
                - "path/to/cert.pem": 使用自定义证书文件
                - "certifi": 使用certifi包的证书
        """
        self.config = config
        
        # SSL配置
        self.ssl_verify = ssl_verify
        self._setup_ssl_context()
        
        # ... [其他初始化代码保持不变] ...
    
    def _setup_ssl_context(self):
        """设置SSL上下文"""
        if self.ssl_verify is True:
            # 使用系统默认证书
            self.requests_verify = True
            print("🔒 SSL验证: 使用系统默认证书")
            
        elif self.ssl_verify is False:
            # 禁用SSL验证（不推荐）
            self.requests_verify = False
            import warnings
            warnings.filterwarnings("ignore", message="Unverified HTTPS request")
            print("⚠️  SSL验证: 已禁用（不安全！）")
            
        elif isinstance(self.ssl_verify, str):
            if self.ssl_verify.lower() == "certifi":
                # 使用certifi包的证书
                self.requests_verify = certifi.where()
                print(f"🔒 SSL验证: 使用certifi证书 ({self.requests_verify})")
            else:
                # 使用自定义证书文件
                if os.path.exists(self.ssl_verify):
                    self.requests_verify = self.ssl_verify
                    print(f"🔒 SSL验证: 使用自定义证书 ({self.ssl_verify})")
                else:
                    print(f"⚠️  证书文件不存在: {self.ssl_verify}，回退到系统证书")
                    self.requests_verify = True
        
        else:
            # 默认使用系统证书
            self.requests_verify = True
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> Optional[requests.Response]:
        """
        发送API请求（增强版：支持SSL验证）
        """
        # 确保令牌有效
        if not self._ensure_token_valid():
            return None
        
        # 构建完整URL
        if endpoint.startswith("http"):
            url = endpoint
        else:
            url = f"{self.graph_endpoint}{endpoint}"
        
        # 设置SSL验证
        if "verify" not in kwargs:
            kwargs["verify"] = self.requests_verify
        
        # 设置超时
        if "timeout" not in kwargs:
            kwargs["timeout"] = 30
        
        try:
            # 发送请求
            response = requests.request(
                method=method,
                url=url,
                headers=self.headers,
                **kwargs
            )
            
            # ... [原有的错误处理逻辑保持不变] ...
            
        except requests.exceptions.SSLError as e:
            print(f"❌ SSL证书验证失败: {str(e)}")
            print("   解决方案:")
            print("   1. 检查系统证书是否过期")
            print("   2. 使用 certifi: ssl_verify='certifi'")
            print("   3. 或提供自定义证书文件路径")
            return None
        except requests.exceptions.Timeout:
            print(f"❌ 请求超时: {url}")
            return None
        except requests.exceptions.RequestException as e:
            print(f"❌ 请求异常: {str(e)}")
            return None
            
# 使用系统默认证书（生产环境推荐）
client = SharePointClient(config, ssl_verify=True)

# 安装certifi
pip install certifi
# 使用certifi提供的证书
client = SharePointClient(config, ssl_verify="certifi")

# 使用自定义证书文件
client = SharePointClient(config, ssl_verify="/path/to/your/certificate.pem")


# 获取证书并保存为PEM格式
openssl s_client -connect login.microsoftonline.com:443 -showcerts </dev/null 2>/dev/null | \
    openssl x509 -outform PEM > microsoft_cert.pem

# 获取证书链中的所有证书
openssl s_client -connect login.microsoftonline.com:443 -showcerts </dev/null | \
    sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p' > chain.pem
    
    
import ssl
import socket
import certifi

def save_ssl_certificate(hostname, port=443, filename="certificate.pem"):
    """下载服务器的SSL证书并保存到文件"""
    try:
        # 创建SSL上下文
        context = ssl.create_default_context()
        
        # 连接到服务器
        with socket.create_connection((hostname, port)) as sock:
            with context.wrap_socket(sock, server_hostname=hostname) as ssock:
                # 获取证书
                cert_binary = ssock.getpeercert(binary_form=True)
                
                # 将DER格式转换为PEM格式
                cert_pem = ssl.DER_cert_to_PEM_cert(cert_binary)
                
                # 保存到文件
                with open(filename, 'w') as f:
                    f.write(cert_pem)
                
                print(f"✅ 证书已保存到: {filename}")
                return filename
                
    except Exception as e:
        print(f"❌ 获取证书失败: {str(e)}")
        return None

# 使用示例
cert_file = save_ssl_certificate("login.microsoftonline.com", 443, "microsoft_cert.pem")
if cert_file:
    client = SharePointClient(config, ssl_verify=cert_file)
    
    
import os

def create_client_based_on_environment():
    """根据环境创建客户端"""
    
    # 基础配置
    config = {
        "tenant_id": os.getenv("SHAREPOINT_TENANT_ID"),
        "client_id": os.getenv("SHAREPOINT_CLIENT_ID"),
        "client_secret": os.getenv("SHAREPOINT_CLIENT_SECRET"),
        "site_hostname": os.getenv("SHAREPOINT_SITE_HOSTNAME"),
        "site_path": os.getenv("SHAREPOINT_SITE_PATH")
    }
    
    # 根据环境变量决定SSL策略
    environment = os.getenv("APP_ENVIRONMENT", "production").lower()
    
    ssl_configs = {
        "development": {
            "description": "开发环境 - 使用certifi证书",
            "ssl_verify": "certifi"
        },
        "testing": {
            "description": "测试环境 - 可能使用自签名证书",
            "ssl_verify": "/path/to/test/ca_cert.pem"  # 或 False（不推荐）
        },
        "production": {
            "description": "生产环境 - 使用系统证书",
            "ssl_verify": True
        }
    }
    
    if environment in ssl_configs:
        ssl_config = ssl_configs[environment]
        print(f"🌍 环境: {environment}")
        print(f"📋 {ssl_config['description']}")
        
        # 创建客户端
        client = SharePointClient(config, ssl_verify=ssl_config["ssl_verify"])
        return client
    else:
        print(f"⚠️  未知环境: {environment}，使用生产环境配置")
        return SharePointClient(config, ssl_verify=True)

# 使用示例
client = create_client_based_on_environment()

----------------------------------------------------
class SecureSharePointClient(SharePointClient):
    """增强安全性的SharePoint客户端"""
    
    def __init__(self, config: Dict[str, str], ssl_verify: Union[bool, str] = True):
        super().__init__(config, ssl_verify)
        
        # 添加额外的安全设置
        self._enhance_security()
    
    def _enhance_security(self):
        """增强安全性设置"""
        import warnings
        
        # 禁止不安全的SSL警告（仅在验证时）
        if self.requests_verify:
            warnings.filterwarnings("default", category=UserWarning)
        else:
            warnings.filterwarnings("ignore", message="Unverified HTTPS request")
        
        # 设置更安全的SSL上下文
        if isinstance(self.ssl_verify, (bool, str)) and self.ssl_verify not in [False, "certifi"]:
            self.ssl_context = self._create_secure_ssl_context()
    
    def _create_secure_ssl_context(self):
        """创建安全的SSL上下文"""
        import ssl
        
        context = ssl.create_default_context()
        
        # 设置安全协议版本
        context.minimum_version = ssl.TLSVersion.TLSv1_2
        
        # 禁用弱加密套件
        context.set_ciphers('ECDHE+AESGCM:ECDHE+CHACHA20:DHE+AESGCM:DHE+CHACHA20:'
                           '!aNULL:!eNULL:!EXPORT:!DES:!RC4:!3DES:!MD5:!PSK')
        
        return context
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> Optional[requests.Response]:
        """重写请求方法以使用安全SSL上下文"""
        
        # 如果创建了自定义SSL上下文，使用它
        if hasattr(self, 'ssl_context') and self.ssl_context:
            kwargs.setdefault('verify', self.ssl_verify)
            
            # 对于自定义证书文件，需要特殊处理
            if isinstance(self.ssl_verify, str) and self.ssl_verify != "certifi":
                # 这里可以使用更高级的证书验证逻辑
                pass
        
        return super()._make_request(method, endpoint, **kwargs)
----------------------------------------------------------------------
def test_ssl_configuration():
    """测试SSL配置是否正确"""
    
    test_urls = [
        "https://login.microsoftonline.com",
        "https://graph.microsoft.com",
        "https://company.sharepoint.com"  # 替换为您的实际地址
    ]
    
    ssl_options = [
        (True, "系统证书"),
        ("certifi", "certifi证书"),
        (False, "禁用验证（不安全）")
    ]
    
    print("🔍 开始SSL配置测试...")
    print("=" * 60)
    
    for ssl_verify, description in ssl_options:
        print(f"\n测试配置: {description}")
        
        for test_url in test_urls:
            try:
                start_time = time.time()
                response = requests.get(test_url, timeout=10, verify=ssl_verify)
                elapsed = (time.time() - start_time) * 1000
                
                if response.status_code == 200:
                    print(f"  ✅ {test_url}: 成功 ({elapsed:.0f}ms)")
                else:
                    print(f"  ⚠️  {test_url}: HTTP {response.status_code}")
                    
            except requests.exceptions.SSLError as e:
                print(f"  ❌ {test_url}: SSL错误 - {str(e)[:80]}")
            except Exception as e:
                print(f"  ❌ {test_url}: 错误 - {str(e)[:80]}")
    
    print("\n" + "=" * 60)
    print("✅ SSL测试完成")

# 运行测试
if __name__ == "__main__":
    test_ssl_configuration()
--------------------------------------------------------
# 生产环境最佳实践示例
def create_production_client():
    """创建生产环境客户端（最佳实践）"""
    
    # 1. 从安全存储加载配置（如AWS Secrets Manager、Azure Key Vault）
    config = load_config_from_vault()
    
    # 2. 验证所有必需的配置都存在
    required_keys = ["tenant_id", "client_id", "client_secret", "site_hostname", "site_path"]
    for key in required_keys:
        if not config.get(key):
            raise ValueError(f"缺少必需的配置: {key}")
    
    # 3. 创建客户端（生产环境强制使用SSL验证）
    client = SharePointClient(config, ssl_verify=True)
    
    # 4. 记录安全配置
    logger.info(f"SharePoint客户端已初始化，SSL验证: {'启用' if client.requests_verify else '禁用'}")
    
    return client
-------------------------------------------------------------