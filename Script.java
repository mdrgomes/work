package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileReader;
import java.util.Iterator; 
import java.util.Map;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.util.Objects;
import java.util.Scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject; 
import org.json.simple.parser.*;

public class Script{

	private static String userGithub;
	private static String tokenGithub;
	private static String userJenkins;
	private static String tokenJenkins;

	public static void gitInit(Path directory) throws IOException, InterruptedException {
	runCommand(directory, "git", "init");
	}

	public static void gitPush(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "push");
	}

	public static void gitStage(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "add", "-A");
	}

	public static void gitCommit(Path directory, String message) throws IOException, InterruptedException {
		runCommand(directory, "git", "commit", "-m", message);
	}

	public static void gitClone(Path directory, String originUrl) throws IOException, InterruptedException {
		runCommand(directory.getParent(), "git", "clone", originUrl, directory.getFileName().toString());
	}

	public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {
		Objects.requireNonNull(directory, "directory");
		if (!Files.exists(directory)) {
			throw new RuntimeException("Não foi possivel correr o comando no diretorio '" + directory + "'");
		}
		ProcessBuilder pb = new ProcessBuilder()
				.command(command)
				.directory(directory.toFile());
		Process p = pb.start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR"); 
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
		outputGobbler.start();
		errorGobbler.start();
		int exit = p.waitFor();
		errorGobbler.join();
		outputGobbler.join();
		if (exit != 0) {
			throw new AssertionError(String.format("runCommand returnou %d", exit));
		}
	}

	private static class StreamGobbler extends Thread {

		private final InputStream is;
		private final String type;

		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		@Override
		public void run() {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(type + "> " + line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void Copy(String path){
		FileInputStream instream = null;
	    FileOutputStream outstream = null;
 
    	try{
    	    File infile =new File(path + "/Jenkinsfile_default.txt");
    	    File outfile =new File(path + "/Jenkinsfile");
 
    	    instream = new FileInputStream(infile);
    	    outstream = new FileOutputStream(outfile);
 
    	    byte[] buffer = new byte[1024];
 
    	    int length;
    	   
    	    while ((length = instream.read(buffer)) > 0){
    	    	outstream.write(buffer, 0, length);
    	    }

    	    instream.close();
    	    outstream.close();

    	    System.out.println("Jenkinsfile criado com sucesso!!");
 
    	}catch(IOException ioe){
    		ioe.printStackTrace();
    	 }
	}

	public static void getInput() {
	    JSONParser parser = new JSONParser();
 
        try {
 
            Object obj = parser.parse(new FileReader("input.json"));
 
            JSONObject jsonObject = (JSONObject) obj;
 
            String userGithub = jsonObject.get("userGithub");
            String tokenGithub = jsonObject.get("tokenGithub");
            String userJenkins = jsonObject.get("userJenkins");
            String tokenJenkins = jsonObject.get("tokenJenkins");

             } catch (Exception e) {
            e.printStackTrace();
        }
  	}

	public static void main(String[] args) throws IOException{

		Scanner input =new Scanner(System.in);

		getInput();

		System.out.println("Introduza o nome do repositorio github");
		String gitRep = input.nextLine();
		
		System.out.println("Introduza o localPath, senão tem ainda escreva N ");
		String localPath = input.nextLine();
		
		String originUrl = "https://"+ tokenGithub + "@github.com/" + userGithub + "/" + gitRep + ".git";

		if(localPath.equals("N")){

			System.out.println("Escreva o path para onde vai ficar o clone");
			String director = input.nextLine();

			Path directory = Paths.get(director + "/" + gitRep);
			try {
				gitClone(directory, originUrl);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			localPath = director + "/" + gitRep;
		}

		File tempFile = new File(localPath + "/Jenkinsfile");
		boolean exists = tempFile.exists();
		
		if(exists)
			System.out.println("existe");
		
		else{
			
			System.out.println("");
			System.out.println("A criar Jenkinsfile");
			System.out.println("");
			Copy(localPath); //criar o Jenkinsfile
			System.out.println("");

			//url para fazer push 
			//Path dir = Paths.get(localPath);
			try {
				Path dir = Paths.get(localPath);
				gitInit(dir);
				gitStage(dir);
				System.out.println("Escreva o commit");
				String mensagem = input.nextLine();
				gitCommit(dir, mensagem);
				gitPush(dir);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		System.out.println("Repositorio github verificado");

		System.out.println("");

		System.out.println("Introduza o nome branch");
		String branch = input.nextLine();
		
		String authToken = "1234567890";

		String url = "https://github.com/" + userGithub + "/" + gitRep;

		WriteXMLFile writexmlfile = new WriteXMLFile();

		writexmlfile.WriteXml(originUrl,authToken, url, branch);

		System.out.println("Introduza o nome do Job");
		String jobName = input.nextLine();

        String authStr = userJenkins + ":" + tokenJenkins;
        String encoding = DatatypeConverter.printBase64Binary(authStr.getBytes("utf-8"));

        int responseCodeNewJob = new Script().createJob(jobName, encoding); // criar funçao para mudar valores no config.xml
        System.out.println("The response code is: "+ responseCodeNewJob);
   
   		System.out.println();
		System.out.println("Se deseja terminar digite (exit), se pretende fazer novamente a build digite (build) e se pretende ver o estado da Build digite (status).");
		String texto = "run";
		
		int cont=0;
		while(!texto.equals("exit")){
			System.out.println("Programa a correr");
			texto = input.nextLine();
			if(texto.equals("build")){
				System.out.println("A iniciar build");
				cont++;
				buildJob(jobName, encoding, authToken);
				System.out.println();
			}
			else if(texto.equals("status")){
				if(cont==0)
					System.out.println("Pecisa ser feita Build primeiro.");
				else{
					status(encoding, jobName, cont);
					System.out.println();
				}
			}
			else 
				System.out.println("Comando inválido.");
		}
		System.out.println("Programa a fechar");
	}

	public int createJob(String jobName, String encoding){
        int responseCode=00;
        try {
               
            URL url = new URL("http://localhost:8080/createItem?name=" + jobName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setRequestProperty("Authorization", "Basic " + encoding);

            Scanner scanner = new Scanner( new File("config.xml"), "UTF-8" );
            String input = scanner.useDelimiter("\\A").next();
            scanner.close();

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            responseCode= conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
    
            String output;
            System.out.println("Output do servidor .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            
            conn.disconnect();
        } catch (MalformedURLException e) {        
            e.printStackTrace();        
        } catch (IOException e) {        
            e.printStackTrace();        
        }
        return responseCode;
    }

    public static void buildJob(String jobName, String encoding, String token){
        try{
            URL url = new URL ("http://localhost:8080/job/" + jobName + "/build?token="+ token); //ver o token 
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            
            InputStream content = connection.getInputStream();
            BufferedReader in   = new BufferedReader (new InputStreamReader (content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void status(String encoding, String jobName, int lastBuild){
    	try{
    		URL url = new URL("http://localhost:8080/job/" + jobName + "/"+ lastBuild + "/api/json");
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encoding);

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
             
            String inputLine;
            while ((inputLine = br.readLine()) != null) {     
                JSONObject a = (JSONObject) parser.parse(inputLine);     
        
                    Object value = a.get("result");
                    Object building = a.get("building");
                    Object estimated_time = a.get("estimatedDuration");

                    System.out.println("a"+building+"b");

                    String build = "" + building;

                    if(build.equals("true"))
                        System.out.println("Build em progresso, tempo estimado "+ estimated_time);
                    else 
                        System.out.println("Build status " + value);
                    
    
            }
            br.close();
    	}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }   
    }
} 