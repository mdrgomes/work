package src;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class WriteXMLFile {

	static void WriteXml(String urlgit, String token, String originUrl, String branch) {

	  try {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("flow-definition");
		doc.appendChild(rootElement);

		Attr attr = doc.createAttribute("plugin");
		attr.setValue("workflow-job@2.25");
		rootElement.setAttributeNode(attr);

		Element actions = doc.createElement("actions");
		rootElement.appendChild(actions);

		// declarativeJobAction elements
		Element declarativeJobAction = doc.createElement("org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction");
		actions.appendChild(declarativeJobAction);

		Attr attr1 = doc.createAttribute("plugin");
		attr1.setValue("pipeline-model-definition@1.3.2");
		declarativeJobAction.setAttributeNode(attr1);

		//keepDependecies elements
		Element keepDependencies = doc.createElement("keepDependencies");
		keepDependencies.appendChild(doc.createTextNode("false"));
		rootElement.appendChild(keepDependencies);

		// properties elements
		Element properties = doc.createElement("properties");
		rootElement.appendChild(properties);

		// GithubProjectProperty elements
		Element githubProjectProperty = doc.createElement("com.coravy.hudson.plugins.github.GithubProjectProperty");
		properties.appendChild(githubProjectProperty);

		Attr attr2 = doc.createAttribute("plugin");
		attr2.setValue("github@1.29.3");
		githubProjectProperty.setAttributeNode(attr2);

		//projectUrl
		Element projectUrl = doc.createElement("projectUrl");
		projectUrl.appendChild(doc.createTextNode(originUrl));
		githubProjectProperty.appendChild(projectUrl);

		//displayName
		Element displayName = doc.createElement("displayName");
		githubProjectProperty.appendChild(displayName);

		//definition elements
		Element definition = doc.createElement("definition");
		rootElement.appendChild(definition);

		Attr attr3 = doc.createAttribute("class");
		attr3.setValue("org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition");
		definition.setAttributeNode(attr3);		

		Attr attr4 = doc.createAttribute("plugin");
		attr4.setValue("workflow-cps@2.60");
		definition.setAttributeNode(attr4);

		//scm elements
		Element scm = doc.createElement("scm");
		definition.appendChild(scm);

		Attr attr5 = doc.createAttribute("class");
		attr5.setValue("hudson.plugins.git.GitSCM");
		scm.setAttributeNode(attr5);

		Attr attr6 = doc.createAttribute("plugin");
		attr6.setValue("git@3.9.1");
		scm.setAttributeNode(attr6);

		//configVersion
		Element configVersion = doc.createElement("configVersion");
		configVersion.appendChild(doc.createTextNode("2"));
		scm.appendChild(configVersion);

		//userRemoteConfigs
		Element userRemoteConfigs = doc.createElement("userRemoteConfigs");
		scm.appendChild(userRemoteConfigs);

		//userRemoteConfigs
		Element git_userRemoteConfigs = doc.createElement("hudson.plugins.git.UserRemoteConfig");
		userRemoteConfigs.appendChild(git_userRemoteConfigs);

		//url
		Element url= doc.createElement("url");
		url.appendChild(doc.createTextNode(urlgit));//inserir o url aqui
		git_userRemoteConfigs.appendChild(url);

		//branches
		Element branches= doc.createElement("branches");
		scm.appendChild(branches);

		//BranchSpec
		Element branchSpec= doc.createElement("hudson.plugins.git.BranchSpec");
		branches.appendChild(branchSpec);

		//name
		Element name= doc.createElement("name");
		name.appendChild(doc.createTextNode(branch));
		branchSpec.appendChild(name);

		//doGenerateSubmoduleConfigurations
		Element doGenerateSubmoduleConfigurations= doc.createElement("doGenerateSubmoduleConfigurations");
		doGenerateSubmoduleConfigurations.appendChild(doc.createTextNode("false"));
		scm.appendChild(doGenerateSubmoduleConfigurations);

		//submoduleCfg
		Element submoduleCfg= doc.createElement("submoduleCfg");
		scm.appendChild(submoduleCfg);

		Attr attr7 = doc.createAttribute("class");
		attr7.setValue("list");
		submoduleCfg.setAttributeNode(attr7);

		//extensions
		Element extensions= doc.createElement("extensions");
		scm.appendChild(extensions);

		//scriptPath
		Element scriptPath= doc.createElement("scriptPath");
		scriptPath.appendChild(doc.createTextNode("Jenkinsfile"));
		definition.appendChild(scriptPath);

		//lightweight
		Element lightweight= doc.createElement("lightweight");
		lightweight.appendChild(doc.createTextNode("true"));
		definition.appendChild(lightweight);

		//triggers
		Element triggers= doc.createElement("triggers");
		rootElement.appendChild(triggers);
 
		//authToken
		Element authToken= doc.createElement("authToken");
		authToken.appendChild(doc.createTextNode(token)); //colocar o tokende autentificaçao
		rootElement.appendChild(authToken);

		//disabled
		Element disabled= doc.createElement("disabled");
		disabled.appendChild(doc.createTextNode("false")); 
		rootElement.appendChild(disabled);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("config.xml"));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("Ficheiro config.xml foi criado!");

	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
	}
}