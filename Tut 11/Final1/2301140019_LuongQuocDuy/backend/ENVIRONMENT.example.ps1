# Copy values into the PowerShell terminal that runs the backend. No personal secrets here.
$env:DB_USERNAME='root'
$env:DB_PASSWORD='your-local-mysql-password'
$env:DB_URL='jdbc:mysql://localhost:3306/demo_2301140019?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:SERVER_PORT='8080'
$env:FRONTEND_ORIGINS='http://localhost:3000,http://127.0.0.1:3000'
