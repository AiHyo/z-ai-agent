<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-logo">
        <span class="footer-logo-text">AI Agent</span>
      </div>
      <div class="footer-links">
        <a href="https://github.com/AiHyo" target="_blank" class="footer-link github-link">
          <svg viewBox="0 0 24 24" class="github-icon" width="16" height="16" fill="currentColor">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          GitHub
        </a>
        <span class="footer-copyright">
          © {{ currentYear }}
        </span>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'TheFooter',
  computed: {
    currentYear() {
      return new Date().getFullYear();
    }
  }
}
</script>

<style scoped>
.footer {
  padding: 10px 0;
  font-size: 0.8rem;
  opacity: 0.8;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.footer-logo {
  margin-right: 10px;
}

.footer-logo-text {
  font-weight: bold;
  font-size: 1rem;
  background: linear-gradient(to right, #3a86ff, #ff006a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

.footer-copyright {
  opacity: 0.7;
}

.github-link {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  color: #fff;
  text-decoration: none;
}

.github-link:hover {
  color: #6e5494;
  transform: translateY(-2px);
}

.github-icon {
  vertical-align: middle;
}
</style> 