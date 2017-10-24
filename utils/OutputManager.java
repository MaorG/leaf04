package leaf04.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import leaf04.Leaf04Context;
import leaf04.PhysicalAgent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;
import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;

public class OutputManager {

	String outputRelativePath;
	long runId;
	int interval;
	Expr intervalExpression;
	String intervalExpressionStr;
	private Map<String, String> parametersMap;
	private Map<String, String> outputTypeMap;
	private Vector<String> tickEventLog;
	Boolean isKeepingLog;
	Leaf04Context context;

	public OutputManager(Leaf04Context context, Node outputNode, Node simulationNode) {

		parametersMap = new HashMap<String, String>();
		outputTypeMap = new HashMap<String, String>();
		tickEventLog = new Vector<String>();

		this.context = context;
		Node runIdNode = ParserUtils.getNodeByTagName(simulationNode, "runId");
		this.runId = Integer.parseInt( runIdNode.getTextContent()); 


		String relativePath = ParserUtils.getStringByName(outputNode, "outputPath");

		// read interval
		int interval = ParserUtils.getIntByName(outputNode, "interval");
		String temp;
		temp = ParserUtils.getStringByName(outputNode, "intervalExpression");
		if (temp != null) {
			intervalExpressionStr = temp;
			try {
				intervalExpression = Parser.parse(intervalExpressionStr);
			} catch (SyntaxException e) {
				System.err.println(e.explain());
				e.printStackTrace();
				return;
			}
		}
		else {
			intervalExpressionStr = null;
			intervalExpression = null;
		}

		try {
			setOutputRelativePath(relativePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		setInterval(interval);

		// read parameters
		Node parametersNode = ParserUtils.getNodeByTagName(simulationNode, "parameters");
		mapParamsFromNode(parametersNode, parametersMap);
		Node domainNode = ParserUtils.getNodeByTagName(simulationNode, "domain");
		mapParamsFromNode(domainNode, parametersMap);
		
		
		// config data output:
		Node outputTypeNode = ParserUtils.getNodeByTagName(outputNode, "outputType");
		outputTypeMap.put("parameters", "true");
		if (outputTypeNode != null) {
			// get type of data to output
			mapParamsFromNode(outputTypeNode, outputTypeMap);
		}
		else {
			// default data output:
			outputTypeMap.put("parameters", "true");
			outputTypeMap.put("agents", "true");
		}


		Integer outputLog = ParserUtils.getIntByName(outputNode, "outputLog");
		isKeepingLog = !(outputLog == null || outputLog == 0);
		createLogFile();
	}

	public void addEventStringToLog(String eventStr) {
		if (isKeepingLog)
			tickEventLog.add(eventStr);
	}

	public void createLogFile() {
		if (!isKeepingLog) {
			return;
		}
		String fileName = "simlog";

		String eol = System.getProperty("line.separator");


		IndexedIterable<PhysicalAgent> allAgents = context.getObjects(Object.class);



		String text;
		BufferedWriter output = null;

		try {
			File dir = new File(outputRelativePath  + Integer.toString((int) runId));
			if (!dir.exists()) {
				if (dir.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!   " + outputRelativePath  + Integer.toString((int) runId));
				}
			}


			File file = new File(outputRelativePath + Integer.toString((int) runId) + "/" + fileName + ".log");
			output = new BufferedWriter(new FileWriter(file));
			// todo: move this line..
			parametersMap.put("runId", Integer.toString((int) runId));


			output.write("{parameters}" + eol);
			for (Entry<String, String> entry : parametersMap.entrySet()) {
				text = entry.getKey();
				output.write(text + ", ");
			}
			output.write(eol);
			for (Entry<String, String> entry : parametersMap.entrySet()) {
				text = entry.getValue();
				output.write(text + ", ");
			}
			output.write(eol);

			output.write("{events}" + eol);
			output.write("tick, " + PhysicalAgent.getGeoHeader() +", event"+ eol);

			output.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		} 
	}

	public void appendLogFile(int ticks) {
		if (!isKeepingLog) {
			return;
		}
		String fullFileName = outputRelativePath + Integer.toString((int) runId) + "/" + "simlog" + ".log";
		BufferedWriter bw = null;

		try {
			// APPEND MODE SET HERE

			bw = new BufferedWriter(new FileWriter(fullFileName, true));
			for (String entry : tickEventLog) {
				String tickAndEntry = Integer.toString(ticks) + ", " + entry;
				bw.write(tickAndEntry);
				bw.newLine();				
			}
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {                       // always close the file

			tickEventLog.clear();		// and clear the events vector
			if (bw != null) try {
				bw.close();

			} catch (IOException ioe2) {
				// just ignore it
			}
		} // end try/catch/finally
	}


	// TODO move to another file (parser utils?) - this is quite general and can be further generalized
	public void mapParamsFromNode(Node parametersNode,  Map<String, String> parametersMap) {
		if (parametersNode != null) {
			List<Node> parametersNodeList = ParserUtils.getNodesByTagName(parametersNode, "param");

			// create solutes
			for (int i = 0; i < parametersNodeList.size(); i++) {
				Element e = (Element)parametersNodeList.get(i);
				String parameterName = e.getAttribute("name");
				String parameterValue = ((Element)parametersNodeList.get(i)).getTextContent();

				parametersMap.put(parameterName, parameterValue);
			}
		}
	}

	public void setOutputRelativePath(String outputRelativePath) throws IOException {
		this.outputRelativePath = outputRelativePath;		
		File dir = new File(outputRelativePath);
		if (!dir.exists()) {
			if (dir.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!   " + outputRelativePath);
			}
		} 

		dir = new File(outputRelativePath  + Integer.toString((int) runId));
		if (!dir.exists()) {
			if (dir.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!   " + outputRelativePath  + Integer.toString((int) runId));
			}
		}


	}

	public void setInterval(int interval) {
		this.interval = interval;		
	}

	public void output(int ticks) {
		
		appendLogFile(ticks);
		if (intervalExpression != null) {
			Variable T = Variable.make("T");
			
			double stepTime = this.context.stepTime;
			double totalTime = ticks*stepTime;
			
			
			T.setValue(totalTime);
			double res = intervalExpression.value();
			if (res > 0) {
				doOutput(ticks);
				System.out.printf("T: %d\n", ticks);
			}
		}
		else {
			
			double stepTime = this.context.stepTime;
			
			double totalTime = ticks*stepTime;
			
			// get which interval
			int totalTimeI = (int) Math.floor(totalTime / this.interval);
			
			// get delta between interval start time and current time
			double delta = totalTime - totalTimeI * this.interval;
			
			// do output if first tick in interval
			if (delta < stepTime) {
//			if (ticks%interval == 0) {
				doOutput(totalTimeI);
			}
		}
	}

	void outputParameters(BufferedWriter output, int intervalNum) {

		String text;
		String eol = System.getProperty("line.separator");

		int ticks = (int) this.context.getTicks();
		
		parametersMap.put("ticks", Integer.toString(ticks));
		parametersMap.put("interval", Integer.toString(this.interval));
		parametersMap.put("time", Double.toString(ticks * this.context.stepTime));
		parametersMap.put("intervalNum", Integer.toString(intervalNum));
		// todo: move this line..
		parametersMap.put("runId", Integer.toString((int) runId));


		try {
			output.write("{parameters}" + eol);

			for (Entry<String, String> entry : parametersMap.entrySet()) {
				text = entry.getKey();
				output.write(text + ", ");
			}
			output.write(eol);
			for (Entry<String, String> entry : parametersMap.entrySet()) {
				text = entry.getValue();
				output.write(text + ", ");
			}
			output.write(eol);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void outputAgents(BufferedWriter output) {

		String text;
		String eol = System.getProperty("line.separator");


		IndexedIterable<PhysicalAgent> allAgents = context.getObjects(Object.class);

		try {
			output.write("{agents}" + eol);
			text = PhysicalAgent.getGeoHeader();
			output.write(text + eol);
			for (PhysicalAgent b : allAgents) {
				text = b.getGeo();
				output.write(text + eol);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void outputSolutes(BufferedWriter output) {
		
		String text;
		String eol = System.getProperty("line.separator");
		
		try {
			output.write("{solutes}" + eol);
	
			Map<String, ValueLayerDiffuser> diffuserMap = context.getDiffuserMap();
			for (Entry<String, ValueLayerDiffuser> entry : diffuserMap.entrySet()) {
				text = entry.getKey();
				ValueLayer vl = context.getValueLayer(text);;
	
				int width = (int) vl.getDimensions().getWidth();
				int height = (int) vl.getDimensions().getHeight();
	
				output.write(text + ", " + Integer.toString(width) + ", " + Integer.toString(height) + ", " + eol);
	
	
				for (int j = 0; j < height; j++) {
					text = "";
					for (int i = 0; i < width; i++) {
						text = text + vl.get(i,j);
						if (i < width - 1) {
							text = text + ", ";
						}
					}
					output.write(text + eol);
				}
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
void outputPopulation(BufferedWriter output) {
		
		String text;
		String eol = System.getProperty("line.separator");
		
		IndexedIterable<PhysicalAgent> allAgents = context.getObjects(Object.class);
		
		Map<String, Integer> popMap = new HashMap<String, Integer>();;

		try {
			output.write("{population}" + eol);
			for (PhysicalAgent b : allAgents) {
				String key = b.speciesName + (b.attached ? "1" : "0");

				Integer value = popMap.get(key);
				if (value == null){
					value = 0;
					popMap.put(key, 0);
				}
				popMap.put(key, value + 1);
				
			}
			
			for (Entry<String, Integer> entry : popMap.entrySet()) {
				text = entry.getKey();
				output.write(text + ", ");
			}			
			output.write(eol);
			for (Entry<String, Integer> entry : popMap.entrySet()) {
				text = entry.getValue().toString();
				output.write(text + ", ");
			}
			output.write(eol);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void doOutput(int ticks) {
		String fileName = "output" + Integer.toString(ticks);
		String eol = System.getProperty("line.separator");


		String text;
		BufferedWriter output = null;

		try {
			File dir = new File(outputRelativePath  + Integer.toString((int) runId));
			if (!dir.exists()) {
				if (dir.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!   " + outputRelativePath  + Integer.toString((int) runId));
				}
			}


			File file = new File(outputRelativePath + Integer.toString((int) runId) + "/" + fileName + ".txt");
			output = new BufferedWriter(new FileWriter(file));


			String value = null;
			// output parameters
			value = (String) outputTypeMap.get("parameters");
			if (value != null && value.equals("true")) {
				outputParameters(output, ticks);
			}

			// output agents
			value = (String) outputTypeMap.get("agents");
			if (value != null && value.equals("true")) {
				outputAgents(output);
			}

			// output solutes
			value = (String) outputTypeMap.get("solutes");
			if (value != null && value.equals("true")) {
				outputSolutes(output);
			}

			// output population
			value = (String) outputTypeMap.get("population");
			if (value != null && value.equals("true")) {
				outputPopulation(output);
			}


			output.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		} 
	}

	public void writeFileFromNode(Node simulationNode, String fileName) {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Node importedNode = doc.importNode(simulationNode, true);
			doc.appendChild(importedNode);
			//doc.appendChild(simulationNode);

			TransformerFactory transformerFactory =
					TransformerFactory.newInstance();
			Transformer transformer =
					transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result =
					new StreamResult(new File(outputRelativePath + Integer.toString((int) runId) + "/"  + fileName ));
			transformer.transform(source, result);
			// Output to console for testing
			StreamResult consoleResult =
					new StreamResult(System.out);
			transformer.transform(source, consoleResult);


		} catch (ParserConfigurationException | TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// root elements


	}
}
