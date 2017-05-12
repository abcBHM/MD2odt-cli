# MD2odt-cli
This is CLI application for MD2odt library. For more informations see MD2odt GitHub page https://github.com/abcBHM/MD2odt

Here is how to use this application:  
  
```
java -jar MD2odt.jar path_to_source path_to_output [-t path_to_template] [-c user_JVM_supported_charset]
java -jar MD2odt.jar help    
Order of required arguments has to be respected.
```
  
Use example:  
```
java -jar MD2odt-cli.jar C:\Users\Vita\Desktop\resources C:\Users\Vita\Desktop\out.odt 
-t C:\Users\Vita\Desktop\resources\template.odt -c iso-8859-2
```