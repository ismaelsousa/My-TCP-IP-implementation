/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ismae
 */
public class Testethread {

    public static void main(String[] args) {
        ArrayList i = new ArrayList();
        i.add(new String("aaa"));
        i.add(new String("bbb"));
        i.add(new String("ccc"));

        
        System.out.println(i);
        Threadparave t = new Threadparave(i);
        t.start();

        try {
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Testethread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(i);
        

    }

}
