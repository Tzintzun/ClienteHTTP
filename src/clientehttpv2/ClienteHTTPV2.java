/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientehttpv2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author david
 */
public class ClienteHTTPV2 {

    public  URL url;
    Integer hilos = 0;
    ExecutorService poolThread = Executors.newFixedThreadPool(100);
    
    class Manejador implements Runnable{
        URLConnection conexion;
        String carpetas;
        public Manejador(URLConnection con, String dir){
            conexion = con;
            carpetas = dir;
        }
        @Override
        public void run() {
            try {
                synchronized(hilos){
                    hilos += 1;
                }
                DataInputStream dis = new DataInputStream(conexion.getInputStream());
                List<String> recursos = extraerRecursos(crearIndex(carpetas,dis));
                wget(carpetas,recursos);
                synchronized(hilos){
                    hilos -= 1;
                    System.out.println(hilos);
                }
                
                return;
            } catch (IOException ex) {
                Logger.getLogger(ClienteHTTPV2.class.getName()).log(Level.SEVERE, null, ex);
                synchronized(hilos){
                    hilos -= 1;
                    System.out.println(hilos);
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteHTTPV2.class.getName()).log(Level.SEVERE, null, ex);
                synchronized(hilos){
                    hilos -= 1;
                    System.out.println(hilos);
                }
            }
        
        
            System.out.println("Finalizo el hilo");
        }     
    }
    
    public List<String> extraerRecursos(File f) throws FileNotFoundException, IOException{
        Pattern pt =Pattern.compile("(?i)HREF\\s*=\\s*\"(.*?)\"");
        Pattern pt2 = Pattern.compile("(?i)SRC\\s*=\\s*\"(.*?)\"");
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String archivo ="";
        String linea ;
        while((linea = br.readLine())!=null){
            archivo += linea +"\n";
        }
        Matcher m = pt.matcher(archivo);
        Matcher m2 = pt2.matcher(archivo);
        List<String> recursos = new ArrayList();
        while(m.find()){
            recursos.add(m.group(1));
            
        }
        while(m2.find()){
            recursos.add(m2.group(1));
        }
        
        /*for(String n : recursos){
            System.out.println(n);
        }
        System.out.println(url.getPath());*/
        System.out.println("Finalizado Recursos");
        return recursos;
    }
    
    public void downloadFile(URLConnection conexion, File f) {
        try{
            DataInputStream dis = new DataInputStream(conexion.getInputStream());
            File aux = new File(f.getParent());
            if(!aux.exists()){
                aux.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(f);
            System.out.println("Coemnzado descarga de " + f.getName() + "...");
            while(true){
                try{
                  fos.write(dis.readByte());
                }catch(EOFException eo){
                    break;
                }
            }
            System.out.println(""
                    + "Finzalizada descarga de " + f.getName() + "...");
            dis.close();
            fos.close();
        }catch(Exception e){
            System.out.println("Archivo "+ f.getName()+" no pudo crearse");
            return;
        }
        
    }
    public void wget(String directorioActual,List<String>recursos) throws Exception{
        
        for(String n : recursos){
            if(n.endsWith("/")){
               if(!n.startsWith("/")) {
                try{
                    System.out.println("http://"+url.getHost()+directorioActual.substring(1, directorioActual.length())+n);
                    URL nurl = new URL("http://"+url.getHost()+directorioActual.substring(1, directorioActual.length())+n);
                    
                    poolThread.execute(new Manejador(nurl.openConnection(),directorioActual+n));
                }catch(MalformedURLException mue){
                    System.out.println("Recurso "+n+" no valido");
                    continue;
                }
                
               }
            }else{
                File f = new File(directorioActual+n);
                if(!f.exists()){
                    try{
                        URL furl = new URL("http://"+url.getHost()+directorioActual.substring(1, directorioActual.length())+n);
                        downloadFile(furl.openConnection(), f);
                    }catch(MalformedURLException mue){
                        System.out.println("Recurso "+n+" no valido");
                        continue;
                    }
                }
            }
        }
        System.out.println("finalizado wget");
    }
    public File crearIndex(String direcotrio, DataInputStream dis) throws FileNotFoundException, IOException{
        String diraux;
        if(direcotrio.endsWith("/")){
            diraux = direcotrio;
        }else{
            diraux = direcotrio + "/";
        }
        File dir = new File(diraux);
        System.out.println(diraux);
        //System.out.println(direcotrio+"/");
        if(!dir.exists()){
            for(int i = 0;i<5;i++){
                if(dir.mkdirs()){
                    break;
                }
            }
        }
        File f = new File(diraux +"index.html");
       
        FileOutputStream fos = new FileOutputStream(f);
        while(true){
            try{
              fos.write(dis.readByte());
            }catch(EOFException eo){
                break;
            }
        }
        dis.close();
        System.out.println("Finalizado creacion index");
        return f;
    }
    public ClienteHTTPV2() throws IOException, InterruptedException{
        Scanner teclado = new Scanner(System.in);
       
        //ExecutorService poolThread = Executors.newFixedThreadPool(10);
        while(true){
             System.out.print("Introduce una URL: ");
             
             String buffer = teclado.nextLine();
             
             try{
                 url = new URL(buffer);
                 break;
             }catch (MalformedURLException mue){
                 System.out.println("URL invalida, por favor introduce una URL correcta");
             }
             
        }
        String directorio  = "."+url.getPath();
        File carpetas = new File(directorio);
        if(!carpetas.exists()){
            carpetas.mkdirs();
        }
        
        URLConnection conexion = url.openConnection();
        poolThread.execute(new Manejador(conexion, directorio));
        
        
        
        Thread.sleep(1000);
        while(true){
            synchronized(hilos){
                if(hilos == 0){
                    break;
                }
            }
            Thread.sleep(100);
        }
       poolThread.shutdown();
        System.out.println("Finzlizado constructor");
    }
    public static void main(String[] args) throws Exception {
       ClienteHTTPV2 cliente = new ClienteHTTPV2();
        System.out.println("Finalizado main");
    }
    
}
