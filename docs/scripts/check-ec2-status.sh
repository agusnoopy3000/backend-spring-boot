#!/bin/bash

# Script para verificar el estado del proyecto Spring Boot en EC2
# IP del servidor EC2
EC2_IP="52.2.172.54"
EC2_USER="ec2-user"

echo "🔍 VERIFICANDO ESTADO DE SPRING BOOT EN EC2"
echo "=============================================="
echo ""

echo "📍 Servidor: $EC2_IP"
echo ""

# Función para verificar conectividad
check_connectivity() {
    echo "1️⃣  Verificando conectividad con EC2..."
    if ping -c 1 -W 2 $EC2_IP > /dev/null 2>&1; then
        echo "✅ Servidor accesible"
    else
        echo "❌ No se puede alcanzar el servidor"
        return 1
    fi
    echo ""
}

# Función para verificar proceso Java
check_java_process() {
    echo "2️⃣  Verificando proceso Java en EC2..."
    echo "Ejecutando: ssh $EC2_USER@$EC2_IP 'ps aux | grep java'"
    echo ""
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'ps aux | grep java | grep -v grep'
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Proceso Java encontrado"
    else
        echo ""
        echo "❌ No se encontró proceso Java corriendo"
    fi
    echo ""
}

# Función para verificar puerto 8080
check_port() {
    echo "3️⃣  Verificando puerto 8080..."
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'sudo netstat -tlnp | grep 8080 || sudo ss -tlnp | grep 8080'
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Puerto 8080 está en uso (aplicación escuchando)"
    else
        echo ""
        echo "❌ Puerto 8080 no está en uso"
    fi
    echo ""
}

# Función para verificar logs recientes
check_logs() {
    echo "4️⃣  Últimas 30 líneas del log de aplicación..."
    echo "Ejecutando: ssh $EC2_USER@$EC2_IP 'tail -30 ~/app.log'"
    echo ""
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'tail -30 ~/app.log 2>/dev/null || echo "⚠️  No se encontró archivo app.log"'
    echo ""
}

# Función para verificar health endpoint
check_health_endpoint() {
    echo "5️⃣  Verificando Health Endpoint..."
    echo "Probando: http://$EC2_IP:8080/actuator/health"
    echo ""
    
    response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 http://$EC2_IP:8080/actuator/health)
    
    if [ "$response" = "200" ]; then
        echo "✅ Health endpoint responde OK (200)"
        curl -s http://$EC2_IP:8080/actuator/health | jq . 2>/dev/null || curl -s http://$EC2_IP:8080/actuator/health
    else
        echo "❌ Health endpoint no responde correctamente (HTTP $response)"
    fi
    echo ""
}

# Función para verificar endpoint de prueba
check_hello_endpoint() {
    echo "6️⃣  Verificando endpoint de prueba..."
    echo "Probando: http://$EC2_IP:8080/hello"
    echo ""
    
    response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 http://$EC2_IP:8080/hello)
    
    if [ "$response" = "200" ]; then
        echo "✅ Hello endpoint responde OK (200)"
        curl -s http://$EC2_IP:8080/hello
    else
        echo "❌ Hello endpoint no responde correctamente (HTTP $response)"
    fi
    echo ""
}

# Función para verificar Swagger
check_swagger() {
    echo "7️⃣  Verificando Swagger UI..."
    echo "Probando: http://$EC2_IP:8080/swagger-ui/index.html"
    echo ""
    
    response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 http://$EC2_IP:8080/swagger-ui/index.html)
    
    if [ "$response" = "200" ]; then
        echo "✅ Swagger UI accesible (200)"
    else
        echo "❌ Swagger UI no accesible (HTTP $response)"
    fi
    echo ""
}

# Función para verificar base de datos
check_database() {
    echo "8️⃣  Verificando conexión a MySQL..."
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'sudo systemctl status mysql 2>/dev/null || sudo systemctl status mysqld 2>/dev/null || echo "⚠️  MySQL no gestionado por systemd"'
    echo ""
}

# Función para verificar errores comunes en logs
check_common_errors() {
    echo "9️⃣  Buscando errores comunes en logs..."
    echo ""
    
    echo "🔴 Errores críticos (ERROR):"
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'grep -i "ERROR" ~/app.log 2>/dev/null | tail -5 || echo "No se encontraron errores recientes"'
    echo ""
    
    echo "⚠️  Advertencias (WARN):"
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'grep -i "WARN" ~/app.log 2>/dev/null | tail -5 || echo "No se encontraron advertencias recientes"'
    echo ""
    
    echo "🔌 Problemas de conexión a BD:"
    ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'grep -i "connection" ~/app.log 2>/dev/null | grep -i "error\|fail\|refused" | tail -3 || echo "No se encontraron problemas de conexión"'
    echo ""
}

# Función para mostrar resumen
show_summary() {
    echo ""
    echo "=============================================="
    echo "📊 RESUMEN DE VERIFICACIÓN"
    echo "=============================================="
    echo ""
    echo "Si la aplicación NO está corriendo, prueba estos comandos en EC2:"
    echo ""
    echo "1. Ver procesos Java:"
    echo "   ssh $EC2_USER@$EC2_IP 'ps aux | grep java'"
    echo ""
    echo "2. Ver logs en tiempo real:"
    echo "   ssh $EC2_USER@$EC2_IP 'tail -f ~/app.log'"
    echo ""
    echo "3. Reiniciar aplicación:"
    echo "   # Primero matar proceso existente"
    echo "   ssh $EC2_USER@$EC2_IP 'pkill -f huertohogar-api'"
    echo ""
    echo "   # Luego iniciar nuevamente"
    echo "   ssh $EC2_USER@$EC2_IP 'cd ~ && nohup java -jar huertohogar-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &'"
    echo ""
    echo "4. Verificar Security Group en AWS:"
    echo "   - Puerto 8080 debe estar abierto"
    echo "   - Verifica en AWS Console > EC2 > Security Groups"
    echo ""
    echo "5. URLs importantes:"
    echo "   - API: http://$EC2_IP:8080"
    echo "   - Health: http://$EC2_IP:8080/actuator/health"
    echo "   - Swagger: http://$EC2_IP:8080/swagger-ui/index.html"
    echo ""
}

# Ejecutar todas las verificaciones
main() {
    check_connectivity || exit 1
    check_java_process
    check_port
    check_logs
    check_health_endpoint
    check_hello_endpoint
    check_swagger
    check_database
    check_common_errors
    show_summary
}

# Ejecutar script
main
