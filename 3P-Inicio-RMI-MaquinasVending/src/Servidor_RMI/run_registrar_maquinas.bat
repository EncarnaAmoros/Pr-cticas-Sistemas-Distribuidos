javac InterfazMaquinaVending.java
set CLASSPATH=C:\Users\Encarna\Documents\Documentoos\Universidad\3Curso\Sistemas Distribuidos\Practicas\3Practica_SD\src\Servidor_RMI
javac MaquinaVending.java
rmic MaquinaVending
jar cvf cliente.jar InterfazMaquinaVending.class MaquinaVending_Stub.class
javac Registro.java
java -Djava.security.policy=registrar.policy Registro 127.0.0.1 1099 Maquina1 Maquina2 Maquina3 Maquina4 Maquina5 Maquina6 Maquina7 Maquina8