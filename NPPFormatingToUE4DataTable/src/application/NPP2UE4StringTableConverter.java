package application;
	
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class NPP2UE4StringTableConverter extends Application {
		
	final static double scene_width = 800;
	final static double scene_height = 800;
	
	final static String color2Type[] = new String[] {
	  			"default", // 0
	  			"multicomments", // 1
	  			"inlinecomments", // 2
	  			"doccomments", // 3
	  			"literals", // 4
	  			"keywords", // 5
	  			"string", // 6
	  			"unused7", // 7 
	  			"unused8", // 8
	  			"unused9", // 9
	  			"signs", // 10
	  			"names", // 11
	  			"unused12", // 12
	  			"unused13", // 13
	  			"unused14", // 14
	  			"unused15", // 15
	  			"scopes", // 16
	  			"annotations", // 17
	  			"unused18", // 18
	  			"unused19", // 19
	  			"unused20", // 20
	  			"unused21", // 21
	  			"unused22", // 22
	  			"unused23", // 23
	  			"numliterals" // 24
	};

	int numSourceColorizations = color2Type.length;

	
	// ########################
	

	

	@Override
	public void start(Stage primaryStage) {
		
		TextFlow textflow = new TextFlow();
		ScrollPane scrollpane = new ScrollPane(textflow);
		Scene scene = new Scene(scrollpane, scene_width, scene_height);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		String output = "";
		
		// get the file selected 
        FileChooser file_chooser = new FileChooser();
        file_chooser.setSelectedExtensionFilter(new ExtensionFilter("HTML files", new String[] {"*.html", "*.htm"}));
        List<File> files = file_chooser.showOpenMultipleDialog(primaryStage); 

        if (files != null) { 
        	
        	for (File file : files) {
              
        		Text text = new Text("parsing " + file.getAbsolutePath() + "\n");
        		text.setStyle("-fx-font-weight: bold");
        		textflow.getChildren().add(text);
        		System.out.println("parsing " + file.getName());

        		
        		FileWriter writer = null;
				try {
					writer = new FileWriter(file.getAbsoluteFile() + ".csv");
					writer.write("---,SourceString\n");
					// writer.write("Key,SourceString\n"); // for String Table export
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		
				if (writer != null) {
        		
					// replace unused source colors with spaces	
	        		for (int sourceColorization = 0; sourceColorization < numSourceColorizations; sourceColorization++){
	        		
	        			output = "";
	        			
	        			String currentClass = "sc" + sourceColorization;
//	        			textflow.getChildren().add(new Text("  working on " + currentClass + "\n"));        			
	        			System.out.println("  working on " + currentClass);
	        			
	        			// write key to file
	        			output += color2Type[sourceColorization].replace(" ", "");			
	        			//output += "\"" + color2Type[sourceColorization].replace(" ", "") + "\""; // for UE4 String Table
	        			//output += '"' + currentClass + '"'			
	        			output += ',';
	
	        			output += '"';
	        			
	        			Document doc = null;
						try {
							doc = Jsoup.parse(file, null);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        			
						List<Node> content = null;
						try {
	        			content = doc.select("div").first().childNodes();
						} catch (NullPointerException e) {
							text = new Text("    excepted structures not found in file, skipped.\n");
							text.setStyle("-fx-fill:red");
							textflow.getChildren().add(text);
			
							return;
						}
	        			
						if (content != null) {
														
		        			String sourceString = "";
		        			
							for(int contentIndex = 0; contentIndex < content.size(); contentIndex++){		        		

		        				String theText = ((Element) content.get(contentIndex)).wholeText();
		        				        				       				
		        				String classname = content.get(contentIndex).attr("class");
			        			System.out.println("    white out " + contentIndex + " of " + content.size() + " elements sc" + sourceColorization + " " + file.getName());
			        			
		        				if (! classname.equals(currentClass) )
		        					theText = theText.replaceAll("[^\n]", " ");	
		        				
	//	        				for (int i = 0; i < theText.length(); i++) {
	//	        					System.out.print((int)theText.charAt(i) + "|");
	//	        				}
	//	        				System.out.println();
		        				
//		        				// UE4 String Table Format
//		        				// replacements to fit UE4 format
//		        				theText = theText.replace("\\", "\\\\"); // \ -> \\
//		        				theText = theText.replaceAll("\\r\\n|\\r|\\n", "\\\\r\\\\n"); //"\\r\\n"); // \n ->\r\n
//		        				theText = theText.replace("\"", "\\\"\""); // " -> \""			
//		        				theText = theText.replace("'", "\\'"); // ' -> \'	
		        				
		        				// UE4 Data Table Format
		        				// replacements to fit UE4 format
		        				theText = theText.replaceAll("\\r\\n|\\r|\\n", "\r\n"); // \n ->\r\n  							        			
		        				theText = theText.replace("\"", "\"\""); //   replace quotation marks:  " -> ""								
		
		        				sourceString += theText;
		        					
		        			}
		        			
							// replace space and tab before \r\n
		        			sourceString = sourceString.replaceAll("[ \t]+(\r\n)", "$1");
		        			
		        			// remove sequences of \r\n before end of text
		        			sourceString = sourceString.replaceAll("[\r\n]+$", "");
		        				        		   		
		        			// insert space at position 0 in empty lines ( for UE4 Text Render Actor )
		        			sourceString = sourceString.replaceAll("\n\r", "\n \r"); // keep attention: \n\r instead of \r\n to grab all case in one run
		        			
		        			// insert space at position 0 in line 1 if the sourceString starts with a newline
		        			if (sourceString.length() > 0 && sourceString.charAt(0) == '\r')
		        				sourceString = " " + sourceString;
		        			
	        				output += sourceString;

		        			output += "\"\n";
		        			textflow.getChildren().add(new Text(output));	                    
							try {
								writer.write(output);
							} catch (IOException e) {
								System.out.println("Write not successful.");
								e.printStackTrace();
							}
		            		
	        			}
	        			
	        		}
	        		
	        		try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        		
				}
        		
        		textflow.getChildren().add(new Text("#################\n"));
        		
        	}
        } 
        
        textflow.getChildren().add(new Text("\nfinished."));

	}
	
}
