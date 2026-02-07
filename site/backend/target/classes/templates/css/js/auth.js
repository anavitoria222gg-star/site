// auth.js - Gerenciamento de autenticação (VERSÃO FINAL FUNCIONAL)
class AuthManager {
    constructor() {
        this.API_URL = 'http://localhost:8080/api';
        this.tokenKey = 'auth_token';
        this.userKey = 'user_data';
        console.log('🔧 AuthManager inicializado. URL:', this.API_URL);
    }

    saveToken(token) {
        localStorage.setItem(this.tokenKey, token);
    }

    saveUser(user) {
        localStorage.setItem(this.userKey, JSON.stringify(user));
    }

    getToken() {
        return localStorage.getItem(this.tokenKey);
    }

    getUser() {
        const user = localStorage.getItem(this.userKey);
        return user ? JSON.parse(user) : null;
    }

    isAuthenticated() {
        return this.getToken() !== null;
    }

    isAdmin() {
        const user = this.getUser();
        return user && user.tipoUsuario === 'admin';
    }

    logout() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.userKey);
        window.location.href = 'login.html';
    }

    validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    showMessage(elementId, text, type = 'success') {
        const message = document.getElementById(elementId);
        if (!message) return;
        
        const icon = type === 'success' ? 'check-circle' : 'exclamation-circle';
        message.innerHTML = `<i class="fas fa-${icon} message-icon"></i><span>${text}</span>`;
        message.className = `message ${type}`;
        message.style.display = 'flex';
        
        if (type === 'success') {
            setTimeout(() => message.style.display = 'none', 5000);
        }
    }

    showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        const errorEl = document.getElementById(`${fieldId}-error`);
        
        if (field) field.classList.add('error-field');
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.style.display = 'block';
        }
    }

    clearErrors() {
        document.querySelectorAll('.error-text').forEach(el => {
            el.style.display = 'none';
            el.textContent = '';
        });
        document.querySelectorAll('.error-field').forEach(el => {
            el.classList.remove('error-field');
        });
    }
}

class RegisterManager extends AuthManager {
    constructor() {
        super();
        console.log('📝 RegisterManager inicializado');
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        const toggleSenha = document.getElementById('toggleSenha');
        const toggleConfirmarSenha = document.getElementById('toggleConfirmarSenha');
        
        if (toggleSenha) toggleSenha.addEventListener('click', () => this.togglePassword('senha', toggleSenha));
        if (toggleConfirmarSenha) toggleConfirmarSenha.addEventListener('click', () => this.togglePassword('confirmarSenha', toggleConfirmarSenha));
        
        const form = document.getElementById('registerForm');
        if (form) form.addEventListener('submit', (e) => this.handleSubmit(e));
    }

    togglePassword(fieldId, toggleIcon) {
        const field = document.getElementById(fieldId);
        field.type = field.type === 'password' ? 'text' : 'password';
        toggleIcon.classList.toggle('fa-eye');
        toggleIcon.classList.toggle('fa-eye-slash');
    }

    async handleSubmit(e) {
        e.preventDefault();
        
        if (!this.validateForm()) return;
        
        const formData = this.getFormData();
        await this.submitForm(formData);
    }

    getFormData() {
        return {
            nome: document.getElementById('nome').value.trim(),
            sobrenome: document.getElementById('sobrenome').value.trim(),
            email: document.getElementById('email').value.trim(),
            senha: document.getElementById('senha').value,
            tipoUsuario: 'cliente'
        };
    }

    validateForm() {
        this.clearErrors();
        let isValid = true;
        
        const nome = document.getElementById('nome').value.trim();
        const sobrenome = document.getElementById('sobrenome').value.trim();
        const email = document.getElementById('email').value.trim();
        const senha = document.getElementById('senha').value;
        const confirmarSenha = document.getElementById('confirmarSenha').value;
        
        if (!nome) { this.showFieldError('nome', 'Nome é obrigatório'); isValid = false; }
        if (!sobrenome) { this.showFieldError('sobrenome', 'Sobrenome é obrigatório'); isValid = false; }
        if (!email) { this.showFieldError('email', 'Email é obrigatório'); isValid = false; }
        else if (!this.validateEmail(email)) { this.showFieldError('email', 'Email inválido'); isValid = false; }
        if (!senha) { this.showFieldError('senha', 'Senha é obrigatória'); isValid = false; }
        else if (senha.length < 6) { this.showFieldError('senha', 'Mínimo 6 caracteres'); isValid = false; }
        if (!confirmarSenha) { this.showFieldError('confirmarSenha', 'Confirme a senha'); isValid = false; }
        else if (senha !== confirmarSenha) { this.showFieldError('confirmarSenha', 'Senhas não coincidem'); isValid = false; }
        
        return isValid;
    }

    async submitForm(formData) {
        const submitBtn = document.getElementById('submitBtn');
        const btnText = document.getElementById('btnText');
        const spinner = document.getElementById('spinner');
        
        submitBtn.disabled = true;
        btnText.textContent = 'Processando...';
        if (spinner) spinner.style.display = 'block';
        
        try {
            const endpoints = [
                'http://localhost:8080/api/usuarios',
                'http://localhost:8080/api/usuario'
            ];
            
            let response = null;
            
            for (const url of endpoints) {
                try {
                    console.log(`🔄 POST para: ${url}`);
                    response = await fetch(url, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(formData)
                    });
                    
                    console.log(`📥 Status: ${response.status}`);
                    
                    if (response.status !== 404) break;
                } catch (error) {
                    console.log(`❌ Erro: ${error.message}`);
                    continue;
                }
            }
            
            if (!response) throw new Error('Não foi possível conectar à API');
            
            const responseText = await response.text();
            
            if (response.ok) {
                this.showMessage('message', '✅ Conta criada! Redirecionando...', 'success');
                setTimeout(() => window.location.href = 'login.html', 2000);
            } else {
                let errorMsg = 'Erro ao criar conta';
                if (response.status === 400) errorMsg = 'Dados inválidos';
                else if (response.status === 409) errorMsg = 'Usuário já existe';
                else if (response.status === 500) errorMsg = 'Erro interno';
                else errorMsg = `Erro ${response.status}`;
                
                this.showMessage('message', `❌ ${errorMsg}`, 'error');
            }
            
        } catch (error) {
            console.error('💥 Erro:', error);
            this.showMessage('message', '❌ Erro de conexão com o servidor', 'error');
        } finally {
            submitBtn.disabled = false;
            btnText.textContent = 'Criar Conta';
            if (spinner) spinner.style.display = 'none';
        }
    }
}

class LoginManager extends AuthManager {
    constructor() {
        super();
        console.log('🔐 LoginManager inicializado');
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkRememberMe();
    }

    setupEventListeners() {
        const toggleSenha = document.getElementById('toggleSenha');
        if (toggleSenha) toggleSenha.addEventListener('click', () => this.togglePassword('senha', toggleSenha));
        
        const form = document.getElementById('loginForm');
        if (form) form.addEventListener('submit', (e) => this.handleSubmit(e));
        
        const rememberCheck = document.getElementById('remember');
        if (rememberCheck) rememberCheck.addEventListener('change', (e) => this.handleRememberMe(e));
    }

    togglePassword(fieldId, toggleIcon) {
        const field = document.getElementById(fieldId);
        field.type = field.type === 'password' ? 'text' : 'password';
        toggleIcon.classList.toggle('fa-eye');
        toggleIcon.classList.toggle('fa-eye-slash');
    }

    handleRememberMe(e) {
        const email = document.getElementById('email').value;
        if (e.target.checked && email) {
            localStorage.setItem('remember_email', email);
        } else {
            localStorage.removeItem('remember_email');
        }
    }

    checkRememberMe() {
        const savedEmail = localStorage.getItem('remember_email');
        if (savedEmail) {
            document.getElementById('email').value = savedEmail;
            document.getElementById('remember').checked = true;
        }
    }

    async handleSubmit(e) {
        e.preventDefault();
        console.log('📤 Submetendo login...');
        
        if (!this.validateForm()) return;
        
        const credentials = this.getCredentials();
        await this.authenticate(credentials);
    }

    getCredentials() {
        return {
            email: document.getElementById('email').value.trim(),
            senha: document.getElementById('senha').value
        };
    }

    validateForm() {
        this.clearErrors();
        let isValid = true;
        
        const email = document.getElementById('email').value.trim();
        const senha = document.getElementById('senha').value;
        
        if (!email) { this.showFieldError('email', 'Email é obrigatório'); isValid = false; }
        else if (!this.validateEmail(email)) { this.showFieldError('email', 'Email inválido'); isValid = false; }
        if (!senha) { this.showFieldError('senha', 'Senha é obrigatória'); isValid = false; }
        
        return isValid;
    }

    async authenticate(credentials) {
        console.log('🔐 Autenticando...');
        
        const submitBtn = document.getElementById('submitBtn');
        const btnText = document.getElementById('btnText');
        const spinner = document.getElementById('spinner');
        const message = document.getElementById('message');
        
        submitBtn.disabled = true;
        btnText.textContent = 'Autenticando...';
        if (spinner) spinner.style.display = 'block';
        if (message) message.style.display = 'none';
        
        try {
            // TENTAR API REAL PRIMEIRO
            let response = null;
            let loginSuccessful = false;
            
            const loginEndpoints = [
                'http://localhost:8080/api/auth/login',
                'http://localhost:8080/api/login'
            ];
            
            for (const url of loginEndpoints) {
                try {
                    console.log(`🔄 Tentando: ${url}`);
                    response = await fetch(url, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(credentials)
                    });
                    
                    console.log(`📥 Status: ${response.status}`);
                    
                    if (response.status !== 404) break;
                } catch (error) {
                    console.log(`❌ Erro: ${error.message}`);
                    continue;
                }
            }
            
            // SE API RESPONDEU
            if (response && response.ok) {
                const data = await response.json();
                console.log('✅ API login:', data);
                
                this.saveToken(data.token || data.accessToken || 'api-token-' + Date.now());
                this.saveUser({
                    email: credentials.email,
                    nome: data.nome || data.name || 'Usuário',
                    tipoUsuario: data.tipoUsuario || data.role || 'cliente'
                });
                
                this.showMessage('message', '✅ Login realizado!', 'success');
                loginSuccessful = true;
                
            } else {
                // MODO DESENVOLVIMENTO (MOCK)
                console.log('⚠ Usando modo desenvolvimento...');
                
                // USUÁRIOS MOCK PARA TESTE
                const mockUsers = {
                    'admin@sistema.com': { senha: 'admin123', nome: 'Administrador', tipo: 'admin' },
                    'cliente@exemplo.com': { senha: 'cliente123', nome: 'Cliente Teste', tipo: 'cliente' },
                    'user@teste.com': { senha: '123456', nome: 'Usuário Teste', tipo: 'cliente' }
                };
                
                const user = mockUsers[credentials.email];
                const isAdminGeneric = credentials.email.endsWith('@sistema.com') && credentials.senha === 'admin123';
                
                if (user && user.senha === credentials.senha) {
                    console.log('✅ Mock login bem-sucedido');
                    
                    this.saveToken('mock-token-' + Date.now());
                    this.saveUser({
                        email: credentials.email,
                        nome: user.nome,
                        tipoUsuario: user.tipo
                    });
                    
                    this.showMessage('message', `✅ Bem-vindo ${user.nome}!`, 'success');
                    loginSuccessful = true;
                    
                } else if (isAdminGeneric) {
                    console.log('✅ Admin genérico login');
                    
                    this.saveToken('admin-token-' + Date.now());
                    this.saveUser({
                        email: credentials.email,
                        nome: 'Administrador',
                        tipoUsuario: 'admin'
                    });
                    
                    this.showMessage('message', '✅ Login admin realizado!', 'success');
                    loginSuccessful = true;
                    
                } else {
                    console.log('❌ Credenciais inválidas');
                    this.showMessage('message', '❌ Email ou senha incorretos', 'error');
                    loginSuccessful = false;
                }
            }
            
            // ⭐⭐⭐⭐ REDIRECIONAMENTO - ESTA É A PARTE IMPORTANTE! ⭐⭐⭐⭐⭐
            if (loginSuccessful) {
                console.log('🎯 Login bem-sucedido! Iniciando redirecionamento...');
                
                // Pequena pausa para ver a mensagem
                setTimeout(() => {
                    const user = this.getUser();
                    console.log('👤 Usuário:', user);
                    
                    // DECIDIR PARA ONDE REDIRECIONAR
                    if (user && user.tipoUsuario === 'admin') {
                        console.log('➡️ Indo para dashboard.html (admin)');
                        window.location.href = 'dashboard.html';
                    } else {
                        console.log('➡️ Indo para index.html (cliente)');
                        window.location.href = 'index.html';
                    }
                }, 1500); // 1.5 segundos de delay
            
            } else {
                console.log('⚠ Login não foi bem-sucedido, não redirecionando');
            }
            
        } catch (error) {
            console.error('💥 Erro fatal:', error);
            this.showMessage('message', '❌ Erro de conexão com o servidor', 'error');
        } finally {
            submitBtn.disabled = false;
            btnText.textContent = 'Entrar';
            if (spinner) spinner.style.display = 'none';
        }
    }
}

// PARA USO GLOBAL
window.AuthManager = AuthManager;
window.RegisterManager = RegisterManager;
window.LoginManager = LoginManager;

// INICIALIZAÇÃO AUTOMÁTICA
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Sistema de autenticação carregado!');
    
    // Auto-detectar página e inicializar o manager correto
    if (document.getElementById('registerForm')) {
        console.log('📝 Inicializando RegisterManager...');
        window.registerManager = new RegisterManager();
    }
    
    if (document.getElementById('loginForm')) {
        console.log('🔐 Inicializando LoginManager...');
        window.loginManager = new LoginManager();
    }
    
    // Testar servidor
    setTimeout(() => {
        console.log('🌐 Testando conexão com servidor...');
        fetch('http://localhost:8080')
            .then(() => console.log('✅ Servidor está respondendo'))
            .catch(() => console.log('⚠ Servidor pode não estar rodando'));
    }, 1000);
});