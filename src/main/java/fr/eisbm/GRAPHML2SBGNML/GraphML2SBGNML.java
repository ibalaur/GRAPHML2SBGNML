package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.SBGNBase.Notes;
import org.sbgn.bindings.Sbgn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GraphML2SBGNML {

	private static final String LIST_OF_COLOR_DEFINITIONS_TAG = "listOfColorDefinitions";
	private static final String VALUE_ATTR = "value";
	private static final String ID_LIST_ATTR = "idList";
	private static final String ID_ATTR = "id";
	private static final String GRAPHICS_TAG = "g";
	private static final String FILL_ATTR = "fill";
	private static final String STROKE_WIDTH_ATTR = "strokeWidth";
	private static final String STROKE_ATTR = "stroke";
	private static final String STYLE_TAG = "style";
	private static final String COLOR_DEFINITION_TAG = "colorDefinition";
	private static final String LIST_OF_STYLES_TAG = "listOfStyles";
	private static final String CONSUMPTION = "consumption";
	private static final String PROCESS_CLASS = "process";
	private static final String PRODUCTION = "production";

	private static final String COLOR_PREFIX = "color_";
	private static final String STYLE_PREFIX = "style_";
	private static final String COLOR_ATTR = "color";
	private static final String HEIGHT_ATTR = "height";
	private static final String WIDTH_ATTR = "width";
	private static final String FONTSIZE_ATTR = "fontSize";
	private static final String X_POS_ATTR = "x";
	private static final String Y_POS_ATTR = "y";

	private static final String XMLNS_N2_NS = "xmlns:ns2";
	private static final String XMLNS_NS = "xmlns";
	private static final String XMLNS_BQBIOL_NS = "xmlns:bqbiol";
	private static final String XMLNS_BQMODEL_NS = "xmlns:bqmodel";
	private static final String XMLNS_CELL_DESIGNER_NS = "xmlns:celldesigner";
	private static final String XMLNS_DC_NS = "xmlns:dc";
	private static final String XMLNS_DC_TERMS_NS = "xmlns:dcterms";
	private static final String XMLNS_VCARD_NS = "xmlns:vCard";

	private static final String ANNOTATION_TAG = "annotation";
	private static final String KEY_TAG = "key";
	private static final String NOTES_TAG = "notes";
	private static final String CLONE_TAG = "clone";
	private static final String ORIENTATION_TAG = "orientation";
	private static final String BQMODEL_IS_GRAPHML_TAG = "bqmodel_is";
	private static final String BIOL_IS_GRAPHML_TAG = "biol_is";
	private static final String BQMODEL_IS_TAG = "bqmodel:is";
	private static final String BQBIOL_IS_TAG = "bqbiol:is";
	private static final String NODE_TAG = "node";
	private static final String EDGE_TAG = "edge";
	private static final String DATA_TAG = "data";
	private static final String RDF_LI_TAG = "rdf:li";
	private static final String URL_TAG = "url";
	private static final String Y_POINT_TAG = "y:Point";
	private static final String Y_ARROWS_TAG = "y:Arrows";
	private static final String RDF_RESOURCE_TAG = "rdf:resource";
	private static final String RDF_BAG_TAG = "rdf:Bag";
	private static final String RDF_RDF_TAG = "rdf:RDF";
	private static final String RDF_ABOUT_TAG = "rdf:about";
	private static final String RDF_DESCRIPTION_TAG = "rdf:Description";
	private static final String Y_BORDER_STYLE_TAG = "y:BorderStyle";
	private static final String Y_FILL_TAG = "y:Fill";
	private static final String Y_GEOMETRY_TAG = "y:Geometry";
	private static final String Y_NODE_LABEL_TAG = "y:NodeLabel";
	private static final String Y_GENERIC_NODE_TAG = "y:GenericNode";
	private static final String Y_LINE_STYLE_TAG = "y:LineStyle";

	Sbgn sbgn = new Sbgn();
	Map map = new Map();
	Set<String> colorSet = new HashSet<String>();
	java.util.Map<String, String> colorMap = new HashMap<String, String>();
	java.util.Map<String, SBGNMLStyle> styleMap = new HashMap<String, SBGNMLStyle>();

	/*public static void main(String[] args) {
		GraphML2SBGN sw = new GraphML2SBGN();

		String szInputFile = "";
		String szOutputFile = "Output.SBGN";
		if (args.length == 1) {
			szInputFile = args[0];
		} else if (args.length == 2) {
			szInputFile = args[0];
			szOutputFile = args[1];
		}
		System.out.println(args.length);
		System.out.println(szInputFile);
		System.out.println(szOutputFile);
		if (!szInputFile.equals("")) {
			sw.parseGraphMLFile(szInputFile, szOutputFile);
			System.out.println("Simulation finished...The output SBGN file is stored at " + szOutputFile);
		} else {
			System.out.println("Converter not started. Please specify the input GraphML file and try again.");
		}
		// sw.parseGraphMLFile(FileUtils.IN_YED_METABOLIC_FILE,
		// FileUtils.OUT_SBGN_ED_METABOLIC_FILE);
		// sw.parseGraphMLFile(FileUtils.IN_YED_RECONMAP_FILE,
		// FileUtils.OUT_SBGN_ED_RECOMMAP_FILE);

		// sw.parseGraphMLFile(FileUtils.IN_GRAPHML_GLYCOSLYSIS,
		// FileUtils.OUT_SBGN_GLYCOSLYSIS); // -to check colors
		// sw.parseGraphMLFile(FileUtils.IN_GRAPHML_MAPK_CASCADE,
		// FileUtils.OUT_SBGN_MAPK_CASCADE);
		// sw.parseGraphMLFile(FileUtils.IN_GRAPHML_CENTRAL_PLANT_METABOLISM,
		// FileUtils.OUT_SBGN_CENTRAL_PLANT_METABOLISM);
	}*/
	
	public static void convert(String szInputFileName, String szOutSBGNFile) {
		GraphML2SBGNML gs = new GraphML2SBGNML();
		gs.parseGraphMLFile(szInputFileName, szOutSBGNFile);		
	}

	void parseGraphMLFile(String szInGraphMLFileName, String szOutSBGNFile) {
		try {
			File inputFile = new File(szInGraphMLFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			File outputFile = new File(szOutSBGNFile);

			map.setLanguage("process description");
			sbgn.setMap(map);

			String szNotesTagId = "";
			String szCloneTagId = "";
			String szBqmodelIsTagId = "";
			String szBqbiolIsTagId = "";
			String szAnnotationTagId = "";
			String szNodeURLTagId = "";
			String szOrientationTagId = "";
			NodeList nKeyList = doc.getElementsByTagName(KEY_TAG);
			for (int temp = 0; temp < nKeyList.getLength(); temp++) {
				Node nNode = nKeyList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("attr.name").toLowerCase().equals(NOTES_TAG)) {
						szNotesTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(CLONE_TAG)) {
						szCloneTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(ANNOTATION_TAG)) {
						szAnnotationTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(BQMODEL_IS_GRAPHML_TAG)) {
						szBqmodelIsTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(BIOL_IS_GRAPHML_TAG)) {
						szBqbiolIsTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(ORIENTATION_TAG)) {
						szOrientationTagId = eElement.getAttribute(ID_ATTR);
					} else if ((eElement.getAttribute("attr.name").toLowerCase().equals(URL_TAG))
							&& (eElement.getAttribute("for").toLowerCase().equals(NODE_TAG))) {
						szNodeURLTagId = eElement.getAttribute(ID_ATTR);
					}
				}
			}

			// process nodes:
			NodeList nList = doc.getElementsByTagName(NODE_TAG);

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				Glyph _glyph = new Glyph();

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					// get id of the node/glyph
					String szGlyphId = eElement.getAttribute(ID_ATTR);
					_glyph.setId(szGlyphId);

					// setting the glyph class
					String szYEDNodeType = processNodeList(eElement.getElementsByTagName(Y_GENERIC_NODE_TAG));
					String szGlyphClass = parseYedNodeType(szYEDNodeType);
					_glyph.setClazz(szGlyphClass);

					String szLabel = getNodeListText(eElement.getElementsByTagName(Y_NODE_LABEL_TAG)).trim();
					if (!szLabel.equals("")) {
						// setting the label of the glyph e.g. Coenzyme A..
						Label _label = new Label();
						_label.setText(szLabel);
						_glyph.setLabel(_label);
					}

					// setting the bbox info
					NodeList nlGeometry = eElement.getElementsByTagName(Y_GEOMETRY_TAG);
					Bbox bbox = new Bbox();

					String szHeight = ((Element) (nlGeometry.item(0))).getAttribute(HEIGHT_ATTR);
					String szWidth = ((Element) (nlGeometry.item(0))).getAttribute(WIDTH_ATTR);
					String szXPos = ((Element) (nlGeometry.item(0))).getAttribute(X_POS_ATTR);
					String szYPos = ((Element) (nlGeometry.item(0))).getAttribute(Y_POS_ATTR);

					bbox.setH(Float.parseFloat(szHeight));
					bbox.setW(Float.parseFloat(szWidth));
					bbox.setX(Float.parseFloat(szXPos));
					bbox.setY(Float.parseFloat(szYPos));
					_glyph.setBbox(bbox);

					// getting the fill color info
					String szFillColorId = ((Element) (eElement.getElementsByTagName(Y_FILL_TAG).item(0)))
							.getAttribute(COLOR_ATTR);
					colorSet.add(szFillColorId);

					NodeList nlBorderStyle = eElement.getElementsByTagName(Y_BORDER_STYLE_TAG);
					// getting the border color info
					String szStrokeColorId = ((Element) (nlBorderStyle.item(0))).getAttribute(COLOR_ATTR);
					colorSet.add(szStrokeColorId);

					// getting the stroke width color info
					float fStrokeWidth = Float.parseFloat(((Element) (nlBorderStyle.item(0))).getAttribute(WIDTH_ATTR));

					// getting the stroke width color info
					float fFontSize = Float
							.parseFloat(((Element) (eElement.getElementsByTagName(Y_NODE_LABEL_TAG).item(0)))
									.getAttribute(FONTSIZE_ATTR));

					String szStyleId = STYLE_PREFIX + fStrokeWidth + szFillColorId.replaceFirst("#", "") + fFontSize
							+ szStrokeColorId.replaceFirst("#", "");
					if (!styleMap.containsKey(szStyleId)) {
						styleMap.put(szStyleId,
								new SBGNMLStyle(szStyleId, szFillColorId, szStrokeColorId, fStrokeWidth, fFontSize));
					}
					styleMap.get(szStyleId).addElementIdToSet(szGlyphId);

					// parse data information on notes, annotation, orientation, clone etc.
					NodeList nlDataList = eElement.getElementsByTagName(DATA_TAG);

					Element eltAnnotation = doc.createElement(ANNOTATION_TAG);
					// TODO: to read the namespace from the file
					Element rdfRDF = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", RDF_RDF_TAG);
					eltAnnotation.appendChild(rdfRDF);
					Element rdfDescription = doc.createElement(RDF_DESCRIPTION_TAG);
					rdfRDF.appendChild(rdfDescription);
					rdfDescription.setAttribute(RDF_ABOUT_TAG, "#" + _glyph.getId());

					for (int temp2 = 0; temp2 < nlDataList.getLength(); temp2++) {
						Element _element = ((Element) (nlDataList.item(temp2)));

						// parse notes information
						if (_element.getAttribute(KEY_TAG).equals(szNotesTagId)) {
							_glyph.setNotes(getSBGNNotes(_element));
						}

						// setting the orientation value for the SBGN process
						if (_element.getAttribute(KEY_TAG).equals(szOrientationTagId)) {
							if (_glyph.getClazz().equals(PROCESS_CLASS)) {
								_glyph.setOrientation(_element.getTextContent());
							}
						}

						// parse annotation information
						else if (_element.getAttribute(KEY_TAG).equals(szAnnotationTagId)) {
							String szText = _element.getTextContent();
							if (!szText.equals("")) {
								szText = szText.replaceAll("\"", "");
								String delims = " ";
								String[] tokens = szText.split(delims);
								for (int i = 0; i < tokens.length; i++) {
									String value = tokens[i].substring(tokens[i].indexOf("=") + 1);
									if (tokens[i].contains(XMLNS_N2_NS + "=")) {
										eltAnnotation.setAttribute(XMLNS_N2_NS, value);
									} else if (tokens[i].contains(XMLNS_NS + "=")) {
										eltAnnotation.setAttribute(XMLNS_NS, value);
									}
								}
							}
						}

						// parse namespace information
						else if (_element.getAttribute(KEY_TAG).equals(szNodeURLTagId)) {
							String szText = _element.getTextContent();
							if (!szText.equals("")) {
								szText = szText.replaceAll("\"", "");
								String delims = " ";
								String[] tokens = szText.split(delims);
								for (int i = 0; i < tokens.length; i++) {
									String value = tokens[i].substring(tokens[i].indexOf("=") + 1);

									if (tokens[i].contains(XMLNS_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_NS, value);
									} else if (tokens[i].contains(XMLNS_BQBIOL_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_BQBIOL_NS, value);
									} else if (tokens[i].contains(XMLNS_BQMODEL_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_BQMODEL_NS, value);
									} else if (tokens[i].contains(XMLNS_CELL_DESIGNER_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_CELL_DESIGNER_NS, value);
									} else if (tokens[i].contains(XMLNS_DC_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_DC_NS, value);
									} else if (tokens[i].contains(XMLNS_DC_TERMS_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_DC_TERMS_NS, value);
									} else if (tokens[i].contains(XMLNS_VCARD_NS + "=")) {
										rdfRDF.setAttribute(XMLNS_VCARD_NS, value);
									}
								}
							}
						}

						// parse clone information
						else if (_element.getAttribute(KEY_TAG).equals(szCloneTagId)) {
							if (!_element.getTextContent().equals("")) {
								Label _label = new Label();
								_label.setText(_element.getTextContent());
								Clone _clone = new Clone();
								_clone.setLabel(_label);
								_glyph.setClone(_clone);
							}
						}

						// parse bqmodel:is information
						else if (_element.getAttribute(KEY_TAG).equals(szBqmodelIsTagId)) {
							String szText = _element.getTextContent();
							if (!szText.equals("")) {
								String delimsLine = "[\n]";
								String[] tokens = szText.split(delimsLine);
								for (int i = 0; i < tokens.length; i++) {
									Element elBqtModelIs = doc.createElement(BQMODEL_IS_TAG);
									// add rdf:Bag
									Element eltRDFBag = doc.createElement(RDF_BAG_TAG);
									elBqtModelIs.appendChild(eltRDFBag);
									Element eltRDFLi = doc.createElement(RDF_LI_TAG);
									eltRDFLi.setAttribute(RDF_RESOURCE_TAG, tokens[i]);
									eltRDFBag.appendChild(eltRDFLi);
									rdfDescription.appendChild(elBqtModelIs);
								}
							}
						}

						// parse bqbiol:is information
						else if (_element.getAttribute(KEY_TAG).equals(szBqbiolIsTagId)) {
							String szText = _element.getTextContent();
							if (!szText.equals("")) {
								Element eltBqbiolIs = doc.createElement(BQBIOL_IS_TAG);
								// add rdf:Bag
								Element eltRDFBag = doc.createElement(RDF_BAG_TAG);
								eltBqbiolIs.appendChild(eltRDFBag);

								String delimsLine = "[\n]";
								String[] tokens = szText.split(delimsLine);
								for (int i = 0; i < tokens.length; i++) {
									Element eltRDFLi = doc.createElement(RDF_LI_TAG);
									eltRDFLi.setAttribute(RDF_RESOURCE_TAG, tokens[i]);
									eltRDFBag.appendChild(eltRDFLi);
								}
								rdfDescription.appendChild(eltBqbiolIs);
							}
						}
					}

					Extension _extension = new Extension();
					_extension.getAny().add(eltAnnotation);
					_glyph.setExtension(_extension);
				}

				// add the glyph to the map
				map.getGlyph().add(_glyph);
			}

			// process edges/arcs:
			NodeList nEdgeList = doc.getElementsByTagName(EDGE_TAG);

			for (int temp = 0; temp < nEdgeList.getLength(); temp++) {
				Node nEdge = nEdgeList.item(temp);
				Arc _arc = new Arc();

				if (nEdge.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nEdge;

					// get id of the edge/arc
					String szArcAttributes = getElementAttributes(eElement).trim();

					String szArrowDirection = processNodeList(eElement.getElementsByTagName(Y_ARROWS_TAG));
					String szProcessType = CONSUMPTION;
					boolean bEdgeToBeCorrected = false;
					if (szArrowDirection.contains("delta")) {
						szProcessType = PRODUCTION;
						if (szArrowDirection.contains("source=\"delta\"")) {
							bEdgeToBeCorrected = true;
						}
					}
					_arc.setClazz(szProcessType);

					String delims = "[\t]";
					String szArcId = "", szArcSource = "", szArcTarget = "";
					szArcAttributes = szArcAttributes.replaceAll("\"", "");
					String[] tokens = szArcAttributes.split(delims);

					for (int i = 0; i < tokens.length; i++) {
						if (tokens[i].contains("id=")) {
							szArcId = tokens[i].replaceAll("id=", "");
							_arc.setId(szArcId);
						} else if (tokens[i].contains("source=")) {
							szArcSource = tokens[i].replaceAll("source=", "");
							for (Glyph g : map.getGlyph()) {
								if (g.getId().equals(szArcSource)) {
									if (bEdgeToBeCorrected) {
										_arc.setTarget(g);
									} else {
										_arc.setSource(g);
									}
									break;
								}
							}
						} else if (tokens[i].contains("target=")) {
							szArcTarget = tokens[i].replaceAll("target=", "");
							for (Glyph g : map.getGlyph()) {
								if (g.getId().equals(szArcTarget)) {
									if (bEdgeToBeCorrected) {
										_arc.setSource(g);
									} else {
										_arc.setTarget(g);
									}
									break;
								}
							}
						}
					}

					String szPointInfo = processNodeList(eElement.getElementsByTagName(Y_POINT_TAG));
					if (!szPointInfo.isEmpty()) {
						String szPortXCoord = "", szPortYCoord = "";
						szPointInfo = szPointInfo.replaceAll("\"", "");
						String[] tokensPort = szPointInfo.split(delims);

						for (int i = 0; i < tokensPort.length; i++) {
							if (tokensPort[i].contains("x=")) {
								szPortXCoord = tokensPort[i].replaceAll("x=", "");
							} else if (tokensPort[i].contains("y=")) {
								szPortYCoord = tokensPort[i].replaceAll("y=", "");
							}
						}

						Port _port = new Port();
						_port.setX(Float.parseFloat(szPortXCoord));
						_port.setY(Float.parseFloat(szPortYCoord));

						if (((Glyph) _arc.getSource()).getClazz().equals(PROCESS_CLASS)) {
							setPortInfo(((Glyph) _arc.getSource()), _port);
							_arc.setSource(_port);
						} else if (((Glyph) _arc.getTarget()).getClazz().equals(PROCESS_CLASS)) {
							setPortInfo(((Glyph) _arc.getTarget()), _port);
							_arc.setTarget(_port);
						}
					}

					NodeList nlLineStyle = eElement.getElementsByTagName(Y_LINE_STYLE_TAG);
					// getting the border color info
					String szStrokeColorId = ((Element) (nlLineStyle.item(0))).getAttribute(COLOR_ATTR);
					colorSet.add(szStrokeColorId);

					// getting the stroke width info
					float fStrokeWidth = Float.parseFloat(((Element) (nlLineStyle.item(0))).getAttribute(WIDTH_ATTR));

					String szStyleId = STYLE_PREFIX + fStrokeWidth + szStrokeColorId.replaceFirst("#", "");

					if (!styleMap.containsKey(szStyleId)) {
						styleMap.put(szStyleId, new SBGNMLStyle(szStyleId, szStrokeColorId, fStrokeWidth));
					}
					styleMap.get(szStyleId).addElementIdToSet(eElement.getAttribute(ID_ATTR));
				}

				// add the arc to the map
				map.getArc().add(_arc);
			}

			addExtension(doc);

			// write everything to disk
			SbgnUtil.writeToFile(sbgn, outputFile);

			System.out.println(
					"SBGN file validation: " + (SbgnUtil.isValid(outputFile) ? "validates" : "does not validate"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @courtesy to Ludovic Roy
	public Notes getSBGNNotes(Element notes) {
		Notes newNotes = new Notes();
		if (notes != null) {
			newNotes.getAny().add(notes);
			return newNotes;
		}
		return null;
	}

	private void addExtension(Document doc) {
		// add extension data
		Extension ext = new Extension();
		// add render information tag
		Element eltRenderInfo = doc.createElementNS("http://www.sbml.org/sbml/level3/version1/render/version1",
				"renderInformation");
		eltRenderInfo.setAttribute(ID_ATTR, "renderInformation");
		eltRenderInfo.setAttribute("backgroundColor", "#ffffff");
		eltRenderInfo.setAttribute("programName", "graphml2sbgn");
		eltRenderInfo.setAttribute("programVersion", "0.1");

		// add list of colors
		Element eltListOfColor = doc.createElement(LIST_OF_COLOR_DEFINITIONS_TAG);
		eltRenderInfo.appendChild(eltListOfColor);

		int i = 0;
		for (String _color : colorSet) {
			i++;
			colorMap.put(_color, COLOR_PREFIX + i);
		}

		for (Entry<String, String> e : colorMap.entrySet()) {
			Element eltColorId = doc.createElement(COLOR_DEFINITION_TAG);
			eltColorId.setAttribute(ID_ATTR, e.getValue());
			eltColorId.setAttribute(VALUE_ATTR, e.getKey());
			eltListOfColor.appendChild(eltColorId);
		}

		// add list of styles
		Element eltListOfStyles = doc.createElement(LIST_OF_STYLES_TAG);
		eltRenderInfo.appendChild(eltListOfStyles);
		for (Entry<String, SBGNMLStyle> e : styleMap.entrySet()) {
			Element eltStyleId = doc.createElement(STYLE_TAG);
			eltStyleId.setAttribute(ID_ATTR, e.getKey());
			eltStyleId.setAttribute(ID_LIST_ATTR, e.getValue().getElementSet());

			// add graphics of the style
			Element graphics = doc.createElement(GRAPHICS_TAG);
			graphics.setAttribute(FILL_ATTR, colorMap.get(e.getValue().getFillColor()));
			graphics.setAttribute("FONTSIZE_ATTR", Float.toString(e.getValue().getFontSize()));
			graphics.setAttribute(STROKE_ATTR, colorMap.get(e.getValue().getStrokeColor()));
			graphics.setAttribute(STROKE_WIDTH_ATTR, Float.toString(e.getValue().getStrokeWidth()));
			eltStyleId.appendChild(graphics);

			eltListOfStyles.appendChild(eltStyleId);
		}

		ext.getAny().add(eltRenderInfo);

		map.setExtension(ext);
	}

	private void setPortInfo(Glyph glyph, Port port) {
		for (Glyph g : map.getGlyph()) {
			if (g.getId().equals(glyph.getId())) {
				int iPortNo = glyph.getPort().size();
				boolean bFoundPort = false;
				for (int i = 0; i < iPortNo; i++) {
					if ((glyph.getPort().get(i).getX() == port.getX())
							&& (glyph.getPort().get(i).getY() == port.getY())) {
						bFoundPort = true;
						port.setId(glyph.getPort().get(i).getId());
						break;
					}
				}
				if (!bFoundPort) {
					port.setId(glyph.getId() + "." + (iPortNo + 1));
					glyph.getPort().add(port);
				}
				break;
			}
		}
	}

	private String parseYedNodeType(String szType) {
		String szGlyphClass = "";
		if (szType.contains(FileUtils.COM_YWORKS_SBGN_SIMPLE_CHEMICAL)) {
			szGlyphClass = "simple chemical";
		} else if (szType.contains(FileUtils.COM_YWORKS_SBGN_PROCESS)) {
			szGlyphClass = PROCESS_CLASS;
		}
		// Unspecified entity
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_UNSPECIFIED_ENTITY)) {
			szGlyphClass = "unspecified entity";
		}
		// Macromolecule
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_MACROMOLECULE)) {
			szGlyphClass = "macromolecule";
		}
		// Tag
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_TAG)) {
			szGlyphClass = "tag";
		}

		return szGlyphClass;

	}

	private String processNodeList(NodeList nodeList) {
		String szContent = "";
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			Node nNode = nodeList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				szContent = szContent.concat(getElementAttributes(eElement));
			}
		}
		return szContent;
	}

	private String getElementAttributes(Element eElement) {
		String szAttributeValues = "";
		for (int i = 0; i < eElement.getAttributes().getLength(); i++) {
			szAttributeValues = szAttributeValues.concat(eElement.getAttributes().item(i) + "\t");
		}
		return szAttributeValues;
	}

	private String getNodeListText(NodeList nodeList) {
		String szContent = "";
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			Node nNode = nodeList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				szContent = szContent.concat(nNode.getTextContent());
			}
		}
		return szContent;
	}
}
