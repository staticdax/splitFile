/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Dax
 */
import java.io.*;
import javax.swing.*;
import javax.swing.JProgressBar;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

public class SplitFile {
    static workFileClass workFile;
    static String sourceFileDir;
    static JProgressBar jpbProgress;
    static Semaphore semaphore = new Semaphore(2);
    
    public static void main(String[] args){
        try {
            
            semaphore.acquire(2);
            mainFrame frame = new mainFrame();
            Runnable splitFileThread = new splitFileIntoPartsRunnableClass();
            Thread thread1 = new Thread(frame);
            Thread thread2 = new Thread(splitFileThread);
            frame.setTitle("Split File");
            frame.setSize(400,150);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            //frame.jtfSourceFile.setText("D://baozoumanhuaQQ.rar");
            frame.jtfSpecifyNumber.setText("3");
            //workFile = new workFileClass("D://tmp.dat");
            //workFile.splitFileIntoParts(3);
            thread1.start();
            thread2.start();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(SplitFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static class workFileClass{
        private String source,destination;
        protected File sourceFile;
        protected File destinationFile;
        protected FileInputStream input;
        protected FileOutputStream output;
        protected int progress;
        protected int numberOfSplitFile;
        protected int r;
        
        public workFileClass(String source){
            try {
                this.source = source;
                sourceFile = new File(this.source);
                input = new FileInputStream(sourceFile);
            } catch (FileNotFoundException ex) {
                System.out.println("Source file not found, exit.");
            }
        }
        
        public workFileClass(String source, String destination){
            try{
                this.source = source;
                this.destination = destination;
                sourceFile = new File(this.source);
                destinationFile = new File(this.destination);
                input = new FileInputStream(sourceFile);
                if(destinationFile.exists()){
                    System.out.println("Destination file already exist, exit.");
                    System.exit(0);
                }
                output = new FileOutputStream(destinationFile);
            }
            catch(FileNotFoundException ex){
                System.out.println("Source file not found, exit.");
                System.exit(0);
            }
            catch(Exception ex){
            }
        }
        
        private void copy(){
            try{
                while((r = input.read()) != -1){
                    output.write((byte)r);
                }
                
            }
            catch(IOException ex){
            }
        }
        
        private void splitFileIntoParts(int n){
            int part = 0;
            int byteCopied = 0;
            try{
                FileOutputStream[] smallFileStream = new FileOutputStream[n];
                File[] smallFile = new File[n];
                
                for(int i = 0; i<n; i++ ){
                    smallFile[i] = new File("D://part"+i+".dat");
                    smallFileStream[i] = new FileOutputStream(smallFile[i]);
                    System.out.println("part"+i+" created.");
                }
                
                //copy to split parts
                this.sourceFile.length();
                try{
                    while((r = input.read())!=-1){
                        if(smallFile[part].length()<(this.sourceFile.length()/3)){
                            smallFileStream[part].write((byte)r);
                            /*System.out.print("byteCopied "+byteCopied+ " ");
                            System.out.print("progress "+ progress+ " ");
                            System.out.println("part"+part+" ");
                            System.out.println(smallFile[part].length());*/
                            byteCopied++;
                            progress = (int)((byteCopied*100)/this.sourceFile.length());
                        }
                        else{
                            smallFileStream[part].write((byte)r);
                            part++;
                            byteCopied++;
                            progress = (int)((byteCopied*100)/this.sourceFile.length());
                            //System.out.print("progress "+ progress+ " ");
                        }
                    }
                    progress = 100;
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
                
                
                for(int i = 0; i<n ; i++){
                    smallFileStream[i].close();
                }
            }
            catch(FileNotFoundException ex){
                System.out.println("Source File Not Found.");
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    static class mainFrame extends JFrame implements Runnable{
        protected JLabel jlbSourceFile = new JLabel("Sourece File");
        protected JLabel jlbSpecifyNumber = new JLabel("Specify the number of small files:");
        protected JFileChooser fileChooser;
        protected JTextField jtfSourceFile = new JTextField();
        protected JTextField jtfSpecifyNumber = new JTextField();
        protected JButton jbtBrowse = new JButton("Browse");
        protected JButton jbtStart = new JButton("Start");
        protected JProgressBar jpbProgress = new JProgressBar();
        protected JLabel jlbNote = new JLabel("note: Splited files will be saved as "
                + "part*.dat in D:");
        
        protected JPanel panel1 = new JPanel(new BorderLayout());
        protected JPanel panel2 = new JPanel(new BorderLayout());
        
        public mainFrame(){
            setLayout(new GridLayout(5,1));
            panel1.add(jlbSourceFile, BorderLayout.WEST);
            panel1.add(jtfSourceFile, BorderLayout.CENTER);
            panel1.add(jbtBrowse, BorderLayout.EAST);
            panel2.add(jlbSpecifyNumber, BorderLayout.WEST);
            panel2.add(jtfSpecifyNumber, BorderLayout.CENTER);
            this.add(panel1);
            this.add(panel2);
            this.add(jbtStart);
            this.add(jpbProgress);
            this.add(jlbNote);
            
            jbtStart.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    if(workFile!=null && workFile.sourceFile.exists()){
                        workFile.numberOfSplitFile = Integer.parseInt(jtfSpecifyNumber.getText());
                        System.out.println("workFile created.");
                        semaphore.release(2);
                    }
                    else{
                        System.out.println("workFile not created.Exit.");
                        System.exit(0);
                    }
                }
            });
            
            jbtBrowse.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    fileChooser = new JFileChooser();
                    if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                        workFile = new workFileClass(fileChooser.getSelectedFile().getAbsolutePath());
                        jtfSourceFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
                        System.out.println(workFile.sourceFile.toString());
                        
                    }
                    else
                        System.exit(0);
                }
            });
        }
        
        @Override
        public void run(){
            try {
                semaphore.acquire();
                jpbProgress.setStringPainted(true);
                Thread.sleep(5);
                while(workFile.progress<=100){
                    jpbProgress.setValue(workFile.progress);
                    jpbProgress.setString(workFile.progress+"%");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(SplitFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                semaphore.release();
            }
        }
    }
    
    public static class splitFileIntoPartsRunnableClass implements Runnable{
        @Override
        public void run(){
            try{
                semaphore.acquire();
                workFile.splitFileIntoParts(workFile.numberOfSplitFile);
            }
            catch(InterruptedException ex){
                ex.printStackTrace();
            }
            finally{
                semaphore.release();
            }
        }
    }
}
