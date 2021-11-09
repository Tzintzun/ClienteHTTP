/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diana.david.clientehttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 *
 * @author david
 */
public class ClienteHTTP {
    
    class Manejador implements Runnable{
        URLConnection conexion;
        public Manejador(URLConnection con){
            conexion = con;
        }
        @Override
        public void run() {
            
        }
        
        
        
    }
    
    
    
    public static void main(String[] args) throws IOException {
        Scanner teclado = new Scanner(System.in);
        URL url; 
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
        
        URLConnection conexion  = url.openConnection();
        Reader r = new InputStreamReader(conexion.getInputStream());
        BufferedReader br  = new BufferedReader(r);
        
        File f = new File("index.html");
        FileOutputStream fos = new FileOutputStream(f);
        String linea;
        while((linea = br.readLine()) != null){
            fos.write(linea.getBytes());
        }
    }
}
