// api-config.js - Configurações da API
const APIConfig = {
    baseURL: 'http://localhost:8080',
    endpoints: {
        register: '/api/usuarios',
        login: '/api/auth/login',
        usuarios: '/api/usuarios'
    },
    
    // Testar todos os endpoints possíveis
    testEndpoints: async function() {
        console.log('🔍 Testando endpoints da API...');
        
        const endpoints = [
            '/api/usuarios',
            '/api/usuario',
            '/api/users',
            '/api/user',
            '/usuarios',
            '/usuario'
        ];
        
        const results = [];
        
        for (const endpoint of endpoints) {
            try {
                const response = await fetch(this.baseURL + endpoint, {
                    method: 'GET',
                    mode: 'cors'
                });
                
                results.push({
                    endpoint,
                    status: response.status,
                    ok: response.ok
                });
                
                console.log(`✅ ${endpoint}: ${response.status}`);
                
            } catch (error) {
                results.push({
                    endpoint,
                    status: 'error',
                    error: error.message
                });
                
                console.log(`❌ ${endpoint}: ${error.message}`);
            }
        }
        
        return results;
    },
    
    // Encontrar endpoint correto automaticamente
    discoverEndpoint: async function(method = 'POST') {
        console.log('🔍 Descobrindo endpoint correto...');
        
        const testData = {
            nome: "Teste",
            sobrenome: "Descoberta",
            email: `test${Date.now()}@exemplo.com`,
            senha: "123456",
            tipoUsuario: "cliente"
        };
        
        const endpoints = [
            '/api/usuarios',
            '/api/usuario',
            '/api/users',
            '/api/user',
            '/usuarios',
            '/usuario'
        ];
        
        for (const endpoint of endpoints) {
            try {
                console.log(`🔄 Tentando ${endpoint}...`);
                
                const response = await fetch(this.baseURL + endpoint, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(testData),
                    mode: 'cors'
                });
                
                if (response.status !== 404 && response.status !== 405) {
                    console.log(`🎯 Endpoint encontrado: ${endpoint} (${response.status})`);
                    return endpoint;
                }
                
            } catch (error) {
                continue;
            }
        }
        
        console.log('❌ Nenhum endpoint válido encontrado');
        return null;
    }
};

// Adicionar ao window para acesso global
window.APIConfig = APIConfig;