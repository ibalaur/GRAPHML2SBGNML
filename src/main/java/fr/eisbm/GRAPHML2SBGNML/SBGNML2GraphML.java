package fr.eisbm.GRAPHML2SBGNML;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.SBGNBase.Notes;
import org.sbgn.bindings.Sbgn;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SBGNML2GraphML {
	private static final String GRAPH_DESCRIPTION_ATTR = "d0";
	private static final String PORTGRAPHICS_ATTR = "d1";
	private static final String PORTGEOMETRY_ATTR = "d2";
	private static final String PORTUSERDATA_ATTR = "d3";
	private static final String NODE_NOTES_ATTR = "d4";
	private static final String NODE_ANNOTATIONS_ATTR = "d5";
	private static final String NODE_BQMODELIS_ATTR = "d6";
	private static final String NODE_BIOLIS_ATTR = "d7";
	private static final String NODE_CLONE_ATTR = "d8";
	private static final String NODE_URL_ATTR = "d9";
	private static final String NODE_DESCRIPT_ATTR = "d10";
	private static final String NODE_ORIENTATION_ATTR = "d11";
	private static final String NODE_GRAPHICS_ATTR = "d12";
	private static final String RESOURCES = "d13";
	private static final String EDGE_URL_ATTR = "d14";
	private static final String EDGE_DESCRIPT_ATTR = "d15";
	private static final String EDGE_GRAPHICS_ATTR = "d16";

	private org.sbgn.bindings.Map map;

	// map to hold information on arcs and corresponding ports
	private java.util.Map<String, PortInformation> portMap = new HashMap<String, PortInformation>();

	DirectedGraph<Glyph, Arc> graph;
	Map<String, String> mColorMap = new HashMap<String, String>();
	Map<String, GraphMLStyle> mGlyphStyleMap = new HashMap<String, GraphMLStyle>();

	SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

/*	public static void main(String[] args) throws JAXBException {

		SBGN2GraphML sr = new SBGN2GraphML();

		 sr.parseSBGNFile(FileUtils.IN_SBGN_ED_METABOLIC_FILE, FileUtils.OUT_YED_METABOLIC_FILE);
		// sr.parseSBGNFile(IN_SBGN_ED_RECOMMAP_FILE, OUT_YED_RECONMAP_FILE);
		 sr.parseSBGNFile(FileUtils.IN_GLYCOSLYSIS, FileUtils.OUT_GLYCOSLYSIS); //-to check colors
		 sr.parseSBGNFile(FileUtils.IN_MAPK_CASCADE, FileUtils.OUT_MAPK_CASCADE); 
		 sr.parseSBGNFile(FileUtils.IN_CENTRAL_PLANT_METABOLISM, FileUtils.OUT_CENTRAL_PLANT_METABOLISM);

		// not working well as it contains groups
		// sr.parseSBGNFile(IN_NEURONAL_MUSCLE_SIGNALLING,
		// OUT_NEURONAL_MUSCLE_SIGNALLING);
		// sr.parseSBGNFile(IN_ACTIVATED_STATALPHA, OUT_ACTIVATED_STATALPHA);
		// sr.parseSBGNFile(IN_EPIDERMAL_GROWTH_FACTOR_RECEPTOR_PATHWAY,
		// OUT_EPIDERMAL_GROWTH_FACTOR_RECEPTOR_PATHWAY);
		// sr.parseSBGNFile(IN_INSULIN, OUT_INSULIN); - to check addEdge null pointer
		// sr.parseSBGNFile(IN_INSULIN_SUBMAP_MAPK_CASCADE, OUT_INSULIN_SUBMAP_MAPK_CASCADE); 

		System.out.println("Simulation finished...");
	}*/
	

	public static void convert(String szInputFileName, String szOutFileName) {
		SBGNML2GraphML sg = new SBGNML2GraphML();
		sg.parseSBGNFile(szInputFileName, szOutFileName);
	}

	public Sbgn readFromFile(File f) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn) unmarshaller.unmarshal(f);
		return result;
	}

	public void parseSBGNFile(String szInSBGNFileName, String szOutGraphMLFileName) {

		FileWriter w;
		// our sbgnml file goes in "f"
		File f = new File(szInSBGNFileName);

		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn;
		try {
			sbgn = readFromFile(f);

			// map is a container for the glyphs and arcs
			map = (org.sbgn.bindings.Map) sbgn.getMap();

			try {
				w = new FileWriter(szOutGraphMLFileName);
				graph = new DefaultDirectedGraph<Glyph, Arc>(Arc.class);

				// we can get a list of glyphs (nodes) in this map with getGlyph()
				for (Glyph g : map.getGlyph()) {
					graph.addVertex(g);
				}

				// we can get a list of arcs (edges) in this map with getArc()
				for (Arc a : map.getArc()) {
					Glyph source;
					if (a.getSource() instanceof Port) {
						source = findGlyph(((Port) a.getSource()).getId());
						portMap.put(a.getId(), new PortInformation(((Port) a.getSource()).getId(),
								((Port) a.getSource()).getX(), ((Port) a.getSource()).getY()));
					} else {
						source = findGlyph(((Glyph) a.getSource()).getId());
					}
					Glyph target;

					if (a.getTarget() instanceof Port) {
						target = findGlyph(((Port) a.getTarget()).getId());
						portMap.put(a.getId(), new PortInformation(((Port) a.getTarget()).getId(),
								((Port) a.getTarget()).getX(), ((Port) a.getTarget()).getY()));
					} else {
						target = findGlyph(((Glyph) a.getTarget()).getId());
					}
					graph.addEdge(source, target);
				}

				parseColorsAndStyles();

				try {
					export(w, graph);
				} catch (TransformerConfigurationException | SAXException e) {
					e.printStackTrace();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (JAXBException e2) {
			e2.printStackTrace();
		}
	}

	private void parseColorsAndStyles() {

		if (null != map.getExtension()) {
			for (Element e : map.getExtension().getAny()) {
				if (null != e) {
					for (int i = 0; i < e.getElementsByTagName("colorDefinition").getLength(); i++) {
						Element colorElem = (Element) e.getElementsByTagName("colorDefinition").item(i);
						mColorMap.put(colorElem.getAttribute("id"), colorElem.getAttribute("value"));
					}

					for (int i = 0; i < e.getElementsByTagName("style").getLength(); i++) {
						Element styleElem = (Element) e.getElementsByTagName("style").item(i);
						String szStyleId = styleElem.getAttribute("id");

						Element graphicProperties = (Element) styleElem.getElementsByTagName("g").item(0);

						String szFillColor = mColorMap.get(graphicProperties.getAttribute("fill"));

						int iFontSize = GraphMLStyle.DEFAULT_FONT_SIZE;
						if (!graphicProperties.getAttribute("fontSize").equals("")) {
							iFontSize = Math.round(Float.parseFloat(graphicProperties.getAttribute("fontSize")));
						}

						String szStrokeColor = mColorMap.get(graphicProperties.getAttribute("stroke"));

						String szStrokeWidth = graphicProperties.getAttribute("strokeWidth");

						String[] glyphsList = styleElem.getAttribute("idList").split(" ");

						for (String _glyphId : glyphsList) {
							mGlyphStyleMap.put(_glyphId,
									new GraphMLStyle(szStyleId, szFillColor, iFontSize, szStrokeColor, szStrokeWidth));
						}
					}
				}
			}
		}
	}

	public void export(Writer writer, DirectedGraph<Glyph, Arc> graph)
			throws SAXException, TransformerConfigurationException {
		// Prepare an XML file to receive the GraphML data
		PrintWriter out = new PrintWriter(writer);
		StreamResult streamResult = new StreamResult(out);
		// SAXTransformerFactory factory = (SAXTransformerFactory)
		// SAXTransformerFactory.newInstance();
		TransformerHandler handler = factory.newTransformerHandler();
		Transformer serializer = handler.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.STANDALONE, "no");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		handler.setResult(streamResult);
		handler.startDocument();
		AttributesImpl attr = new AttributesImpl();

		// <graphml>

		// FIXME: Is this the proper way to add this attribute?
		attr.addAttribute("", "", "xmlns:java", "CDATA", "http://www.yworks.com/xml/yfiles-common/1.0/java");
		attr.addAttribute("", "", "xmlns:sys", "CDATA",
				"http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0");
		attr.addAttribute("", "", "xmlns:x", "CDATA", "http://www.yworks.com/xml/yfiles-common/markup/2.0");
		attr.addAttribute("", "", "xmlns:xsi", "CDATA", "http://www.w3.org/2001/XMLSchema-instance");
		attr.addAttribute("", "", "xmlns:y", "CDATA", "http://www.yworks.com/xml/graphml");
		attr.addAttribute("", "", "xmlns:yed", "CDATA", "http://www.yworks.com/xml/yed/3");
		attr.addAttribute("", "", "xsi:schemaLocation", "CDATA",
				"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");

		handler.startElement("http://graphml.graphdrawing.org/xmlns", "", "graphml", attr);
		handler.endPrefixMapping("xsi");

		// <key> for graph description attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Description");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "graph");
		attr.addAttribute("", "", "id", "CDATA", GRAPH_DESCRIPTION_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for portgraphics attribute
		attr.clear();
		attr.addAttribute("", "", "for", "CDATA", "port");
		attr.addAttribute("", "", "id", "CDATA", PORTGRAPHICS_ATTR);
		attr.addAttribute("", "", "yfiles.type", "CDATA", "portgraphics");
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for portgeometry attribute
		attr.clear();
		attr.addAttribute("", "", "for", "CDATA", "port");
		attr.addAttribute("", "", "id", "CDATA", PORTGEOMETRY_ATTR);
		attr.addAttribute("", "", "yfiles.type", "CDATA", "portgeometry");
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for portuserdata attribute
		attr.clear();
		attr.addAttribute("", "", "for", "CDATA", "port");
		attr.addAttribute("", "", "id", "CDATA", PORTUSERDATA_ATTR);
		attr.addAttribute("", "", "yfiles.type", "CDATA", "portuserdata");
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for notes attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "notes");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_NOTES_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for annotation attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Annotation");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_ANNOTATIONS_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for bqmodel_is attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Bqmodel_Is");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_BQMODELIS_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for biol_is attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Biol_Is");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_BIOLIS_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for clone attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Clone");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_CLONE_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for url attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "URL");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_URL_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for description attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Description");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_DESCRIPT_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for orientation attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Orientation");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_ORIENTATION_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for nodegraphics attribute
		attr.clear();
		attr.addAttribute("", "", "for", "CDATA", "node");
		attr.addAttribute("", "", "id", "CDATA", NODE_GRAPHICS_ATTR);
		attr.addAttribute("", "", "yfiles.type", "CDATA", "nodegraphics");
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for resources attribute
		attr.clear();
		attr.addAttribute("", "", "for", "CDATA", "graphml");
		attr.addAttribute("", "", "id", "CDATA", RESOURCES);
		attr.addAttribute("", "", "yfiles.type", "CDATA", "resources");
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for edge url attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "URL");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "edge");
		attr.addAttribute("", "", "id", "CDATA", EDGE_URL_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for edge description attribute
		attr.clear();
		attr.addAttribute("", "", "attr.name", "CDATA", "Description");
		attr.addAttribute("", "", "attr.type", "CDATA", "string");
		attr.addAttribute("", "", "for", "CDATA", "edge");
		attr.addAttribute("", "", "id", "CDATA", EDGE_DESCRIPT_ATTR);
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <key> for edge graphics attribute
		attr.clear();
		attr.addAttribute("", "", "for", "CDATA", "edge");
		attr.addAttribute("", "", "id", "CDATA", EDGE_GRAPHICS_ATTR);
		attr.addAttribute("", "", "yfiles.type", "CDATA", "edgegraphics");
		handler.startElement("", "", "key", attr);
		handler.endElement("", "", "key");

		// <graph>
		attr.clear();
		attr.addAttribute("", "", "edgedefault", "CDATA",
				(graph instanceof DirectedGraph<?, ?>) ? "directed" : "undirected");
		attr.addAttribute("", "", "id", "CDATA", "G");
		handler.startElement("", "", "graph", attr);

		attr.clear();
		attr.addAttribute("", "", "key", "string", GRAPH_DESCRIPTION_ATTR);
		handler.startElement("", "", "data", attr);
		handler.endElement("", "", "data");

		// we can get a list of glyphs (nodes) in this map with getGlyph()
		for (Glyph g : map.getGlyph()) {

			GraphMLStyle _style = new GraphMLStyle();

			if (mGlyphStyleMap.containsKey(g.getId())) {
				_style.setId(mGlyphStyleMap.get(g.getId()).getId());
				_style.setFillColor(mGlyphStyleMap.get(g.getId()).getFillColor());
				_style.setFontSize(mGlyphStyleMap.get(g.getId()).getFontSize());
				_style.setStrokeColor(mGlyphStyleMap.get(g.getId()).getStrokeColor());
				_style.setStrokeWidth(mGlyphStyleMap.get(g.getId()).getStrokeWidth());
			}

			parseGlyph(handler, g, _style);
		}

		// Add all the edges as <edge> elements...
		for (Arc a : map.getArc()) {
			parseArc(handler, a);
		}

		handler.endElement("", "", "graph");

		// <y:Resources/>
		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", RESOURCES);
		handler.startElement("", "", "data", attr);
		attr.clear();
		handler.startElement("", "", "y:Resources", attr);
		handler.endElement("", "", "y:Resources");
		handler.endElement("", "", "data");

		handler.endElement("", "", "graphml");
		handler.endDocument();

		out.flush();
	}

	private void parseArc(TransformerHandler handler, Arc a) throws SAXException {

		Glyph source;
		Glyph target;
		boolean bProcess = false;
		boolean bTag = false;

		if (a.getSource() instanceof Port) {
			source = findNode(((Port) a.getSource()).getId());
		} else {
			source = findNode(((Glyph) a.getSource()).getId());
		}

		if (a.getTarget() instanceof Port) {
			target = findNode(((Port) a.getTarget()).getId());
		} else {
			target = findNode(((Glyph) a.getTarget()).getId());
		}

		if (target.getClazz().equals("process")) {
			bProcess = true;
		}
		
		if ((source.getClazz().equals("tag")) || (target.getClazz().equals("tag"))) {
			bTag = true;
		}

		if ((null != source) && (null != target)) {
			// <edge>
			AttributesImpl attr = new AttributesImpl();
			attr.clear();
			if (null != a.getId()) {
				attr.addAttribute("", "", "id", "CDATA", a.getId());
			}
			attr.addAttribute("", "", "source", "CDATA", source.getId());
			attr.addAttribute("", "", "target", "CDATA", target.getId());

			handler.startElement("", "", "edge", attr);

			// <data key=EDGE_DESCRIPT_ATTR/>
			attr.clear();
			attr.addAttribute("", "", "key", "CDATA", EDGE_DESCRIPT_ATTR);
			handler.startElement("", "", "data", attr);
			handler.endElement("", "", "data");

			// <data key=EDGE_GRAPHICS_ATTR>
			attr.clear();
			attr.addAttribute("", "", "key", "CDATA", EDGE_GRAPHICS_ATTR);
			handler.startElement("", "", "data", attr);

			// <y:PolyLineEdge>
			attr.clear();
			handler.startElement("", "", "y:PolyLineEdge", attr);

			// bend points for ports representation
			if (portMap.containsKey(a.getId())) {
				attr.clear();
				attr.addAttribute("", "", "sx", "CDATA", "0.0");
				attr.addAttribute("", "", "sy", "CDATA", "0.0");
				attr.addAttribute("", "", "tx", "CDATA", "0.0");
				attr.addAttribute("", "", "ty", "CDATA", "0.0");

				handler.startElement("", "", "y:Path", attr);
				attr.clear();
				attr.addAttribute("", "", "x", "CDATA", Float.toString((portMap.get(a.getId()).getX())));
				attr.addAttribute("", "", "y", "CDATA", Float.toString((portMap.get(a.getId()).getY())));
				handler.startElement("", "", "y:Point", attr);
				handler.endElement("", "", "y:Point");
				handler.endElement("", "", "y:Path");

			}
			attr.clear();
			attr.addAttribute("", "", "color", "CDATA", "#000000");
			attr.addAttribute("", "", "type", "CDATA", "line");
			attr.addAttribute("", "", "width", "CDATA", "1.0");
			handler.startElement("", "", "y:LineStyle", attr);
			handler.endElement("", "", "y:LineStyle");

			attr.clear();

			if (a.getClazz().equals("catalysis")) {
				attr.addAttribute("", "", "source", "CDATA", "none");
				attr.addAttribute("", "", "target", "CDATA", "white_circle");
			} else if (true == bTag){
				attr.addAttribute("", "", "source", "CDATA", "none");
				attr.addAttribute("", "", "target", "CDATA", "none");
			}
			else {
				attr.addAttribute("", "", "source", "CDATA", "none");
				if (true == bProcess) {
					attr.addAttribute("", "", "target", "CDATA", "none");
				} else {
					attr.addAttribute("", "", "target", "CDATA", "delta");
				}
			}
			handler.startElement("", "", "y:Arrows", attr);
			handler.endElement("", "", "y:Arrows");

			attr.clear();
			attr.addAttribute("", "", "smoothed", "CDATA", "false");
			handler.startElement("", "", "y:BendStyle", attr);
			handler.endElement("", "", "y:BendStyle");

			handler.endElement("", "", "y:PolyLineEdge");
			handler.endElement("", "", "data");
			handler.endElement("", "", "edge");
		}
	}

	private void parseGlyph(TransformerHandler handler, Glyph g, GraphMLStyle _style) throws SAXException {

		AttributesImpl attr = new AttributesImpl();
		attr.addAttribute("", "", "id", "CDATA", g.getId());
		handler.startElement("", "", "node", attr);

		// add notes key = NOTES_ATTR
		addNotes(handler, g.getNotes());

		// add annotation key = ANNOTATIONS_ATTR
		addAnnotation(handler, g.getExtension());

		// add clone key = NODE_CLONE_ATTR
		addClone(handler, g.getClone());

		// add orientation key = NODE_ORIENTATION_ATTR
		addOrientation(handler, g.getOrientation());

		// Simple chemical
		if (g.getClazz().equals("simple chemical")) {
			parseSBGNElement(handler, g, _style, FileUtils.COM_YWORKS_SBGN_SIMPLE_CHEMICAL);
		}
		// Process
		else if (g.getClazz().equals("process")) {
			parseProcess(handler, g, _style, FileUtils.COM_YWORKS_SBGN_PROCESS);
		}
		// Unspecified entity
		else if (g.getClazz().equals("unspecified entity")) {
			parseSBGNElement(handler, g, _style, FileUtils.COM_YWORKS_SBGN_UNSPECIFIED_ENTITY);
		}

		// Macromolecule
		else if (g.getClazz().equals("macromolecule")) {
			parseSBGNElement(handler, g, _style, FileUtils.COM_YWORKS_SBGN_MACROMOLECULE);
		}
		// Tag
		else if (g.getClazz().equals("tag")) {
			parseSBGNTag(handler, g, _style, FileUtils.COM_YWORKS_SBGN_TAG);
		}

		handler.endElement("", "", "node");

	}

	private void parseSBGNTag(TransformerHandler handler, Glyph g, GraphMLStyle style, String szConfiguration) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		if (g.getLabel() != null) {
			String vertexLabel = g.getLabel().getText().trim();

			// <data>
			attr.clear();
			attr.addAttribute("", "", "key", "CDATA", NODE_GRAPHICS_ATTR);
			handler.startElement("", "", "data", attr);

			// <y:GenericNode>
			attr.clear();
			attr.addAttribute("", "", "configuration", "CDATA", szConfiguration);
			handler.startElement("", "", "y:GenericNode", attr);

			addGeometry(handler, g);
			addFillColor(handler, style);
			addBorderStyle(handler, style);

			// <y:NodeLabel>
			attr.clear();
			attr.addAttribute("", "", "alignment", "CDATA", "center");
			attr.addAttribute("", "", "autoSizePolicy", "CDATA", "content");
			attr.addAttribute("", "", "fontFamily", "CDATA", "Dialog");
			attr.addAttribute("", "", "fontSize", "CDATA", String.valueOf(style.getFontSize()));
			attr.addAttribute("", "", "fontStyle", "CDATA", "plain");
			attr.addAttribute("", "", "hasBackgroundColor", "CDATA", "false");
			attr.addAttribute("", "", "hasLineColor", "CDATA", "false");
			attr.addAttribute("", "", "height", "CDATA", Float.toString(g.getBbox().getH()));
			attr.addAttribute("", "", "horizontalTextPosition", "CDATA", "center");
			attr.addAttribute("", "", "iconTextGap", "CDATA", "4");
			attr.addAttribute("", "", "modelName", "CDATA", "custom");
			attr.addAttribute("", "", "textColor", "CDATA", "#000000");
			attr.addAttribute("", "", "verticalTextPosition", "CDATA", "center");
			attr.addAttribute("", "", "visible", "CDATA", "true");
			attr.addAttribute("", "", "width", "CDATA", Float.toString(g.getBbox().getW()));
			attr.addAttribute("", "", "x", "CDATA", Float.toString(g.getBbox().getX()));
			attr.addAttribute("", "", "y", "CDATA", Float.toString(g.getBbox().getY()));

			// Content for <y:NodeLabel>
			handler.startElement("", "", "y:NodeLabel", attr);
			handler.characters(vertexLabel.toCharArray(), 0, vertexLabel.length());
			addLabelModel(handler);
			addModelParameter(handler);
			handler.endElement("", "", "y:NodeLabel");

			if ( g.getOrientation().equals("right")) {
				attr.clear();
				handler.startElement("", "", "y:StyleProperties", attr);
				attr.addAttribute("", "", "class", "CDATA", "java.lang.Boolean");
				attr.addAttribute("", "", "name", "CDATA", "com.yworks.sbgn.style.inverse");
				attr.addAttribute("", "", "value", "CDATA", "true");
				handler.startElement("", "", "y:Property", attr);
				handler.endElement("", "", "y:Property");
				handler.endElement("", "", "y:StyleProperties");
			}

			handler.endElement("", "", "y:GenericNode");
			handler.endElement("", "", "data");
		}

	}

	private void parseProcess(TransformerHandler handler, Glyph g, GraphMLStyle style, String szConfiguration)
			throws SAXException {
		// <node>
		AttributesImpl attr = new AttributesImpl();

		// <data>
		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_GRAPHICS_ATTR);
		handler.startElement("", "", "data", attr);

		// <y:GenericNode>
		attr.clear();
		attr.addAttribute("", "", "configuration", "CDATA", szConfiguration);
		handler.startElement("", "", "y:GenericNode", attr);

		addGeometry(handler, g);
		addFillColor(handler, style);
		addBorderStyle(handler, style);

		// <y:NodeLabel>
		attr.clear();
		attr.addAttribute("", "", "alignment", "CDATA", "center");
		attr.addAttribute("", "", "autoSizePolicy", "CDATA", "content");
		attr.addAttribute("", "", "bottomInset", "CDATA", "0");
		attr.addAttribute("", "", "fontFamily", "CDATA", "Dialog");
		attr.addAttribute("", "", "fontSize", "CDATA", String.valueOf(style.getFontSize()));
		attr.addAttribute("", "", "fontStyle", "CDATA", "plain");
		attr.addAttribute("", "", "hasBackgroundColor", "CDATA", "false");
		attr.addAttribute("", "", "hasLineColor", "CDATA", "false");
		attr.addAttribute("", "", "hasText", "CDATA", "false");
		attr.addAttribute("", "", "height", "CDATA", "0.0");
		attr.addAttribute("", "", "horizontalTextPosition", "CDATA", "center");
		attr.addAttribute("", "", "iconTextGap", "CDATA", "4");
		attr.addAttribute("", "", "leftInset", "CDATA", "0");
		attr.addAttribute("", "", "modelName", "CDATA", "custom");
		attr.addAttribute("", "", "rightInset", "CDATA", "0");
		attr.addAttribute("", "", "textColor", "CDATA", "#000000");
		attr.addAttribute("", "", "topInset", "CDATA", "0");
		attr.addAttribute("", "", "verticalTextPosition", "CDATA", "center");
		attr.addAttribute("", "", "visible", "CDATA", "true");
		attr.addAttribute("", "", "width", "CDATA", "0.0");

		// Content for <y:NodeLabel>
		handler.startElement("", "", "y:NodeLabel", attr);
		addLabelModel(handler);
		addModelParameter(handler);
		handler.endElement("", "", "y:NodeLabel");

		handler.endElement("", "", "y:GenericNode");
		handler.endElement("", "", "data");
	}

	private void parseSBGNElement(TransformerHandler handler, Glyph g, GraphMLStyle style, String szConfiguration)
			throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		if (g.getLabel() != null) {
			String vertexLabel = g.getLabel().getText();

			// <data>
			attr.clear();
			attr.addAttribute("", "", "key", "CDATA", NODE_GRAPHICS_ATTR);
			handler.startElement("", "", "data", attr);

			// <y:GenericNode>
			attr.clear();
			attr.addAttribute("", "", "configuration", "CDATA", szConfiguration);
			handler.startElement("", "", "y:GenericNode", attr);

			addGeometry(handler, g);
			addFillColor(handler, style);
			addBorderStyle(handler, style);

			// <y:NodeLabel>
			attr.clear();
			attr.addAttribute("", "", "alignment", "CDATA", "center");
			attr.addAttribute("", "", "autoSizePolicy", "CDATA", "content");
			attr.addAttribute("", "", "fontFamily", "CDATA", "Dialog");
			attr.addAttribute("", "", "fontSize", "CDATA", String.valueOf(style.getFontSize()));
			attr.addAttribute("", "", "fontStyle", "CDATA", "plain");
			attr.addAttribute("", "", "hasBackgroundColor", "CDATA", "false");
			attr.addAttribute("", "", "hasLineColor", "CDATA", "false");
			attr.addAttribute("", "", "height", "CDATA", Float.toString(g.getBbox().getH()));
			attr.addAttribute("", "", "horizontalTextPosition", "CDATA", "center");
			attr.addAttribute("", "", "iconTextGap", "CDATA", "4");
			attr.addAttribute("", "", "modelName", "CDATA", "custom");
			attr.addAttribute("", "", "textColor", "CDATA", "#000000");
			attr.addAttribute("", "", "verticalTextPosition", "CDATA", "center");
			attr.addAttribute("", "", "visible", "CDATA", "true");
			attr.addAttribute("", "", "width", "CDATA", Float.toString(g.getBbox().getW()));
			attr.addAttribute("", "", "x", "CDATA", Float.toString(g.getBbox().getX()));
			attr.addAttribute("", "", "y", "CDATA", Float.toString(g.getBbox().getY()));

			// Content for <y:NodeLabel>
			handler.startElement("", "", "y:NodeLabel", attr);
			handler.characters(vertexLabel.toCharArray(), 0, vertexLabel.length());
			addLabelModel(handler);
			addModelParameter(handler);
			handler.endElement("", "", "y:NodeLabel");

			handler.endElement("", "", "y:GenericNode");
			handler.endElement("", "", "data");
		}
	}

	private void addModelParameter(TransformerHandler handler) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		handler.startElement("", "", "y:ModelParameter", attr);
		attr.addAttribute("", "", "labelRatioX", "CDATA", "0.0");
		attr.addAttribute("", "", "labelRatioY", "CDATA", "0.0");
		attr.addAttribute("", "", "nodeRatioX", "CDATA", "0.0");
		attr.addAttribute("", "", "nodeRatioY", "CDATA", "0.0");
		attr.addAttribute("", "", "offsetX", "CDATA", "0.0");
		attr.addAttribute("", "", "offsetY", "CDATA", "0.0");
		attr.addAttribute("", "", "upX", "CDATA", "0.0");
		attr.addAttribute("", "", "upY", "CDATA", "-1.0");
		handler.startElement("", "", "y:SmartNodeLabelModelParameter", attr);
		handler.endElement("", "", "y:SmartNodeLabelModelParameter");
		handler.endElement("", "", "y:ModelParameter");
	}

	private void addLabelModel(TransformerHandler handler) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		handler.startElement("", "", "y:LabelModel", attr);
		attr.addAttribute("", "", "distance", "CDATA", "4.0");
		handler.startElement("", "", "y:SmartNodeLabelModel", attr);
		handler.endElement("", "", "y:SmartNodeLabelModel");
		handler.endElement("", "", "y:LabelModel");
	}

	private void addGeometry(TransformerHandler handler, Glyph g) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		attr.addAttribute("", "", "height", "CDATA", Float.toString(g.getBbox().getH()));
		attr.addAttribute("", "", "width", "CDATA", Float.toString(g.getBbox().getW()));
		attr.addAttribute("", "", "x", "CDATA", Float.toString(g.getBbox().getX()));
		attr.addAttribute("", "", "y", "CDATA", Float.toString(g.getBbox().getY()));
		handler.startElement("", "", "y:Geometry", attr);
		handler.endElement("", "", "y:Geometry");
	}

	private void addBorderStyle(TransformerHandler handler, GraphMLStyle style) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		attr.addAttribute("", "", "color", "CDATA", style.getStrokeColor());
		attr.addAttribute("", "", "type", "CDATA", "line");
		attr.addAttribute("", "", "width", "CDATA", style.getStrokeWidth());
		handler.startElement("", "", "y:BorderStyle", attr);
		handler.endElement("", "", "y:BorderStyle");
	}

	private void addFillColor(TransformerHandler handler, GraphMLStyle style) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		attr.addAttribute("", "", "color", "CDATA", style.getFillColor());
		attr.addAttribute("", "", "transparent", "CDATA", "false");
		handler.startElement("", "", "y:Fill", attr);
		handler.endElement("", "", "y:Fill");
	}

	private void addOrientation(TransformerHandler handler, String szOrientation) throws SAXException {

		AttributesImpl attr = new AttributesImpl();

		attr.addAttribute("", "", "key", "CDATA", NODE_ORIENTATION_ATTR);
		handler.startElement("", "", "data", attr);

		if (null != szOrientation) {
			handler.characters(szOrientation.toCharArray(), 0, szOrientation.length());
		}
		handler.endElement("", "", "data");
	}

	private void addClone(TransformerHandler handler, Clone clone) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		attr.addAttribute("", "", "key", "CDATA", NODE_CLONE_ATTR);
		handler.startElement("", "", "data", attr);

		if (null != clone) {
			if (null != clone.getLabel()) {
				String cloneInfo = clone.getLabel().getText();
				handler.characters(cloneInfo.toCharArray(), 0, cloneInfo.length());
			}
		}
		handler.endElement("", "", "data");
	}

	private void addAnnotation(TransformerHandler handler, Extension extension) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		String szAnnotation = "";
		String szRDFUrl = "";
		String szRDFDescription = "";
		String szBqmodelIs = "";
		String szBiolIs = "";

		if (null != extension) {
			for (Element e : extension.getAny()) {

				if (null != e) {
					szAnnotation = szAnnotation.concat("xmlns:ns2=\"" + e.getAttribute("xmlns:ns2") + "\" xmlns=\""
							+ e.getAttribute("xmlns") + "\"\n");

					for (int i = 0; i < e.getElementsByTagName("rdf:RDF").getLength(); i++) {

						Element eRDFTag = (Element) e.getElementsByTagName("rdf:RDF").item(i);
						szRDFUrl = szRDFUrl.concat("xmlns:rdf=\"" + eRDFTag.getAttribute("xmlns:rdf") + "\" xmlns=\""
								+ eRDFTag.getAttribute("xmlns") + "\" xmlns:bqbiol=\""
								+ eRDFTag.getAttribute("xmlns:bqbiol") + "\" xmlns:bqmodel=\""
								+ eRDFTag.getAttribute("xmlns:bqmodel") + "\" xmlns:celldesigner=\""
								+ eRDFTag.getAttribute("xmlns:celldesigner") + "\" xmlns:dc=\""
								+ eRDFTag.getAttribute("xmlns:dc") + "\" xmlns:dcterms=\""
								+ eRDFTag.getAttribute("xmlns:dcterms") + "\" xmlns:vCard=\""
								+ eRDFTag.getAttribute("xmlns:vCard") + "\"");
					}

					for (int i = 0; i < e.getElementsByTagName("rdf:Description").getLength(); i++) {

						Element eRDFDEscription = (Element) e.getElementsByTagName("rdf:Description").item(i);
						szRDFDescription = szRDFDescription.concat(eRDFDEscription.getAttribute("rdf:about") + "\n");
					}

					for (int i = 0; i < e.getElementsByTagName("bqmodel:is").getLength(); i++) {
						Element e1 = (Element) e.getElementsByTagName("bqmodel:is").item(i);

						for (int j = 0; j < e1.getElementsByTagName("rdf:li").getLength(); j++) {
							Element e2 = (Element) e1.getElementsByTagName("rdf:li").item(j);
							if (null != e2) {
								szBqmodelIs = szBqmodelIs.concat(e2.getAttribute("rdf:resource") + "\n");
							}
						}
					}

					for (int i = 0; i < e.getElementsByTagName("bqbiol:is").getLength(); i++) {
						Element e1 = (Element) e.getElementsByTagName("bqbiol:is").item(i);
						if (null != e1) {

							for (int j = 0; j < e1.getElementsByTagName("rdf:li").getLength(); j++) {
								Element e2 = (Element) e1.getElementsByTagName("rdf:li").item(j);
								if (null != e2) {
									szBiolIs = szBiolIs.concat(e2.getAttribute("rdf:resource") + "\n");
								}
							}
						}
					}
				}
			}
		}

		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_ANNOTATIONS_ATTR);
		handler.startElement("", "", "data", attr);
		handler.characters(szAnnotation.toCharArray(), 0, szAnnotation.length());
		handler.endElement("", "", "data");

		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_BQMODELIS_ATTR);
		handler.startElement("", "", "data", attr);
		handler.characters(szBqmodelIs.toCharArray(), 0, szBqmodelIs.length());
		handler.endElement("", "", "data");

		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_BIOLIS_ATTR);
		handler.startElement("", "", "data", attr);
		handler.characters(szBiolIs.toCharArray(), 0, szBiolIs.length());
		handler.endElement("", "", "data");

		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_DESCRIPT_ATTR);
		handler.startElement("", "", "data", attr);
		handler.characters(szRDFDescription.toCharArray(), 0, szRDFDescription.length());
		handler.endElement("", "", "data");

		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_URL_ATTR);
		handler.startElement("", "", "data", attr);
		handler.characters(szRDFUrl.toCharArray(), 0, szRDFUrl.length());
		handler.endElement("", "", "data");
	}

	private void addNotes(TransformerHandler handler, Notes notes) throws SAXException {
		AttributesImpl attr = new AttributesImpl();

		attr.clear();
		attr.addAttribute("", "", "key", "CDATA", NODE_NOTES_ATTR);
		handler.startElement("", "", "data", attr);

		if (null != notes) {
			for (Element e : notes.getAny()) {
				String notesInfo = e.getTextContent();
				handler.characters(notesInfo.toCharArray(), 0, notesInfo.length());
			}
		}
		handler.endElement("", "", "data");
	}

	private Glyph findGlyph(String id) {
		for (Glyph g : map.getGlyph()) {
			if (g.getId().equals(id)) {
				return g;
			} else {
				for (Port p : g.getPort()) {
					if (p.getId().equals(id)) {
						return g;
					}
				}
			}
		}
		return null;
	}

	private Glyph findNode(String id) {
		for (Glyph g : graph.vertexSet()) {
			if (g.getId().equals(id)) {
				return g;
			} else {
				for (Port p : g.getPort()) {
					if (p.getId().equals(id)) {
						return g;
					}
				}
			}
		}

		return null;
	}
}
