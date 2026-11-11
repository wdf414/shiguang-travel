<template>
  <footer class="app-footer">
    <div class="footer-wave">
      <svg viewBox="0 0 1200 120" preserveAspectRatio="none">
        <path d="M321.39,56.44c58-10.79,114.16-30.13,172-41.86,82.39-16.72,168.19-17.73,250.45-.39C823.78,31,906.67,72,985.66,92.83c70.05,18.48,146.53,26.09,214.34,3V0H0V27.35A600.21,600.21,0,0,0,321.39,56.44Z" fill="url(#wave-gradient)"></path>
        <defs>
          <linearGradient id="wave-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#667eea;stop-opacity:0.1" />
            <stop offset="50%" style="stop-color:#764ba2;stop-opacity:0.15" />
            <stop offset="100%" style="stop-color:#f093fb;stop-opacity:0.1" />
          </linearGradient>
        </defs>
      </svg>
    </div>
    <div class="footer-gradient"></div>
    <div class="container footer-content">
      <div class="footer-section">
        <h4>
          <span class="icon-wrapper">✈️</span>
          关于我们
        </h4>
        <p>拾光旅行 - 您的专属旅行管家</p>
        <p>提供优质旅游线路和贴心服务</p>
      </div>
      <div class="footer-section">
        <h4>
          <span class="icon-wrapper">📞</span>
          联系方式
        </h4>
        <p class="contact-item">
          <span class="label">客服热线：</span>
          <span class="value">111111(虚拟号码)</span>
        </p>
        <p class="contact-item">
          <span class="label">邮箱：</span>
          <span class="value">xiaohongsu671@gmail.com</span>
        </p>
      </div>
      <div class="footer-section">
        <h4>
          <span class="icon-wrapper">💬</span>
          关注我们
        </h4>
        <p>拾光旅行</p>
        <div class="social-icons">
          <span class="social-icon wechat">📱</span>
          <span class="social-icon weibo">🌐</span>
          <span class="social-icon qq">💬</span>
        </div>
      </div>
    </div>
    <div class="copyright">
      <div class="container">
        <span class="copyright-text">© 2026 畅游天下 版权所有</span>
        <span class="divider">|</span>
        <span class="links">
          <a href="#">隐私政策</a>
          <a href="#">用户协议</a>
        </span>
      </div>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.app-footer {
  background: #323232;
  color: #c0c0c0;
  padding: 50px 0 20px;
  margin-top: 60px;
  font-size: 14px;
}

.footer-wave, .footer-gradient {
  display: none;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #444;
}

.footer-section {
  h4 {
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    .icon-wrapper {
      display: none; // 隐藏之前的图标背景
    }
  }
  
  p {
    line-height: 2;
    margin-bottom: 8px;
    
    &:hover {
      color: #fff;
    }
  }
  
  .contact-item {
    display: flex;
    gap: 10px;
    
    .label {
      color: #999;
    }
    
    .value {
      color: #ccc;
    }
  }
}

.social-icons {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  
  .social-icon {
    width: 32px;
    height: 32px;
    background: #444;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #666;
      color: #fff;
    }
  }
}

.copyright {
  text-align: center;
  color: #666;
  font-size: 12px;
  
  .container {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
  }
  
  .links {
    display: flex;
    gap: 15px;
    
    a {
      color: #666;
      transition: color 0.2s;
      
      &:hover {
        color: #999;
      }
    }
  }
  
  .divider {
    color: #444;
  }
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 30px;
  }
}
</style>
