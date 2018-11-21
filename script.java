import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Objects;

public class script{

	public static void gitPush(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "push");
	}

	public static void gitClone(Path directory, String originUrl) throws IOException, InterruptedException {
		runCommand(directory.getParent(), "git", "clone", originUrl, directory.getFileName().toString());
	}

	public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {
		Objects.requireNonNull(directory, "directory");
		if (!Files.exists(directory)) {
			throw new RuntimeException("can't run command in non-existing directory '" + directory + "'");
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
			throw new AssertionError(String.format("runCommand returned %d", exit));
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


	public static void main(String[] args) throws IOException{
		Scanner input =new Scanner(System.in);
		
		System.out.println("Introduza o url do github");
		String gitUrl = input.nextLine();
		
		System.out.println("Introduza o username");
		String username = input.nextLine();
		
		System.out.println("Introduza o token do github");
		String token =input.nextLine();
		
		System.out.println("Introduza o localPath, senão tem ainda escreva N ");
		String localPath = input.nextLine();
		
		if(localPath == "N"){
			System.out.println("Escreva o link para fazer clone");
			String originUrl = input.nextLine();

			System.out.println("Escreva o path para onde vai ficar o clone");
			String director = input.nextLine();

			Path directory = Paths.get(director);
			try {
				gitClone(directory, originUrl);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			localPath = director;
		}

		File tempFile = new File(localPath + "/JenkinsFile");
		boolean exists = tempFile.exists();
		
		if(exists)
			System.out.println("existe");
		
		else{
			String data = "Test data";
			FileOutputStream out = new FileOutputStream(tempFile);
			out.write(data.getBytes());
			out.close();
			Path dir = Paths.get(localPath);
			try {
				gitPush(dir);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}

} 