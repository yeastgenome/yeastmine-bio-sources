package org.intermine.bio.dataconversion;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.metadata.StringUtil;
import org.intermine.metadata.TypeUtil;
import org.intermine.model.bio.BioEntity;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.util.PropertiesUtil;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ReferenceList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * @author
 */
public class GoAnnotationDbConverter extends BioDBConverter
{

	// configuration maps
	private static final Map<String, String> WITH_TYPES = new LinkedHashMap<String, String>();

	// maps retained across all files
	protected Map<String, String> goTerms = new LinkedHashMap<String, String>();
	private Map<String, String> evidenceCodes = new LinkedHashMap<String, String>();
	private Map<String, String> dataSets = new LinkedHashMap<String, String>();
	private Map<String, String> publications = new LinkedHashMap<String, String>();
	private Map<String, Item> organisms = new LinkedHashMap<String, Item>();
	protected Map<String, String> productMap = new LinkedHashMap<String, String>();
	private Set<String> dbRefs = new HashSet<String>();
	@SuppressWarnings("unused")
	private Map<String, String> databaseAbbreviations = new HashMap<String, String>();
	private Map<String, String> dataSources = new HashMap<String, String>();

	// maps renewed for each file
	private Map<GoTermToGene, Set<Evidence>> goTermGeneToEvidence = new LinkedHashMap<GoTermToGene, Set<Evidence>>();
	private Map<Integer, List<String>> productCollectionsMap  = new LinkedHashMap<Integer, List<String>>();
	private Map<String, Integer> storedProductIds = new HashMap<String, Integer>();

	// These should be altered for different ontologies:
	protected String termClassName = "GOTerm";
	protected String termCollectionName = "goAnnotation";
	protected String annotationClassName = "GOAnnotation";
	private String gaff = "2.0";
	private static final String DEFAULT_ANNOTATION_TYPE = "gene";
	private static final String DEFAULT_IDENTIFIER_FIELD = "primaryIdentifier";

	private static final Logger LOG = Logger.getLogger(GoAnnotationDbConverter.class);

	// 
	private static final String DATASET_TITLE = "SGD curated GO data";
	private static final String DATA_SOURCE_NAME = "SGD";
	private static final GoAnnotationDbProcessor PROCESSOR = new GoAnnotationDbProcessor();
	private Map<String, Item> genes = new HashMap();
	private static final Integer TAXON_ID = 4932;
	private static final Integer HUMAN_TAXON_ID = 9606;
	//private Item organism;

	/**
	 * Constructor
	 *
	 * @param writer the ItemWriter used to handle the resultant items
	 * @param model the Model
	 * @throws Exception if an error occurs in storing or finding Model
	 */
	public GoAnnotationDbConverter(Database database, Model model, ItemWriter writer) throws ObjectStoreException {
		super(database, model, writer);
	}

	/**
	 * Sets the file format for the GAF.  2.0 is the default.
	 *
	 * @param gaff GO annotation file format
	 */
	public void setGaff(String gaff) {
		this.gaff = gaff;
	}


	static {
		WITH_TYPES.put("FB", "Gene");
		WITH_TYPES.put("UniProt", "Protein");
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() throws Exception {
		Connection connection = getDatabase().getConnection();
		processGOAnnotations(connection);
	}



	/**
	 * {@inheritDoc}
	 */
	public void processGOAnnotations(Connection connection) throws SQLException, ObjectStoreException{

		Item organism = newOrganism("4932");

		ResultSet res = PROCESSOR.getGOAnnotations(connection);

		while (res.next()) {	

			String productId = res.getString("sgdid");
			String symbol = res.getString("gene_name");
			String qualifier = res.getString("go_qualifier");
			String goId = res.getString("goid");
			String pub = res.getString("pmid");
			String pubxref = res.getString("sgdrefid");
			String strEvidence = res.getString("evidence_code");
			Array withTextDb = res.getArray("withText");
			String annotType = res.getString("annotation_type");
			String taxonId = parseTaxonId(res.getString("taxid"));
			String dataSourceCode = res.getString("source");
			Array annotExt = res.getArray("annotext");
			Array annotExtPre = res.getArray("annotextension");
			
			String annotationExtension = "";
			String[] pre = (String[])annotExtPre.getArray();
			String[] val = (String[])annotExt.getArray();
			for (int i=0; i< pre.length; i++) {
				if (pre[i] != null &&  val[i] != null && i<(pre.length -1)) {
					annotationExtension += pre[i] +" ("+val[i]+"); ";					
				}else if(pre[i] != null && i<pre.length) {
					annotationExtension += pre[i] +" ("+val[i]+")";
				}
			}

			String[] with = (String[])withTextDb.getArray();
			String withText = "";			
			for (int j=0; j< with.length; j++) {
				if (with[j] != null && j<(with.length -1)) {
					withText += with[j] +",";					
				}else if(with[j] != null && j<with.length) {
					withText += with[j];
				}
			}
					
			if (StringUtils.isNotEmpty(strEvidence)) {
				storeEvidenceCode(strEvidence, annotType, withText);
			} else {
				throw new IllegalArgumentException("Evidence is a required column but not "
						+ "found for goterm " + goId + " and productId " + productId);
			}
			
			GoTermToGene key = new GoTermToGene(productId, goId, qualifier, withText, annotationExtension, pub, pubxref); // create unique key for go annotation

			String dataSource = DATA_SOURCE_NAME;
			String type = "gene";

			String productIdentifier = newProduct(productId, type, organism, dataSource, dataSourceCode, true, "primaryIdentifier");

			
			if (productIdentifier != null) { // null if resolver could not resolve an identifier
				
				String pubRefId = newPublication(pub, pubxref); // null if no pub found
				
				Set<Evidence> allEvidenceForAnnotation = goTermGeneToEvidence.get(key); // get evidence codes for this goterm|gene pair

				// new evidence
				String newStrEvidence = "";
				if(StringUtils.isEmpty(withText)){ 
					newStrEvidence = strEvidence+":"+annotType;
				}else{
					newStrEvidence = strEvidence+":"+annotType+":"+withText;
				}

				if (allEvidenceForAnnotation == null ) { //|| !StringUtils.isEmpty(withText)
					String goTermIdentifier = newGoTerm(goId, dataSource, dataSourceCode);
					Evidence evidence = new Evidence(newStrEvidence, pubRefId, withText, organism,
							dataSource, dataSourceCode); 
					allEvidenceForAnnotation = new LinkedHashSet<Evidence>();
					allEvidenceForAnnotation.add(evidence);
					goTermGeneToEvidence.put(key, allEvidenceForAnnotation);
					Integer storedAnnotationId =  createGoAnnotation(productIdentifier, type,
							goTermIdentifier, organism, qualifier, dataSource, dataSourceCode,
							annotationExtension);
					evidence.setStoredAnnotationId(storedAnnotationId);
				} else {
					boolean seenEvidenceCode = false;
					Integer storedAnnotationId = null;

					for (Evidence evidence : allEvidenceForAnnotation) {
						String evidenceCode = evidence.getEvidenceCode();
						storedAnnotationId = evidence.storedAnnotationId;
						// already have evidence code, just add pub
						if (evidenceCode.equals(newStrEvidence)) {
							evidence.addPublicationRefId(pubRefId);
							seenEvidenceCode = true;
						}
					}
					if (!seenEvidenceCode) {
						Evidence evidence = new Evidence(newStrEvidence, pubRefId, withText, organism,
								dataSource, dataSourceCode);
						evidence.storedAnnotationId = storedAnnotationId;
						allEvidenceForAnnotation.add(evidence);
					}
				}
			}
		}
		storeProductCollections();
		storeEvidence();
	}



	private void storeProductCollections() throws ObjectStoreException {
		for (Map.Entry<Integer, List<String>> entry : productCollectionsMap.entrySet()) {
			Integer storedProductId = entry.getKey();
			List<String> annotationIds = entry.getValue();
			ReferenceList goAnnotation = new ReferenceList(termCollectionName, annotationIds);
			store(goAnnotation, storedProductId);
		}
	}

	private void storeEvidence() throws ObjectStoreException {
		for (Set<Evidence> annotationEvidence : goTermGeneToEvidence.values()) {
			List<String> evidenceRefIds = new ArrayList<String>();
			Integer goAnnotationRefId = null;
			for (Evidence evidence : annotationEvidence) {
				Item goevidence = createItem("GOEvidence");
				goevidence.setReference("code", evidenceCodes.get(evidence.getEvidenceCode()));
				List<String> publicationEvidence = evidence.getPublications();
				if (!publicationEvidence.isEmpty()) {
					goevidence.setCollection("publications", publicationEvidence);
				}

				// with objects
				/* if (!StringUtils.isEmpty(evidence.withText)) {
                    goevidence.setAttribute("withText", evidence.withText);
                    List<String> with = createWithObjects(evidence.withText, evidence.organism,
                            evidence.dataSource, evidence.dataSourceCode);
                    if (!with.isEmpty()) {
                        goevidence.addCollection(new ReferenceList("with", with));
                    }
                }*/
				//goevidence.setAttribute("withText", evidence.withText);
				store(goevidence);
				evidenceRefIds.add(goevidence.getIdentifier());
				goAnnotationRefId = evidence.getStoredAnnotationId();
			}

			ReferenceList refIds = new ReferenceList("evidence",
					new ArrayList<String>(evidenceRefIds));
			store(refIds, goAnnotationRefId);
		}
	}

	private Integer createGoAnnotation(String productIdentifier, String productType,
			String termIdentifier, Item organism, String qualifier, String dataSource,
			String dataSourceCode, String annotationExtension) throws ObjectStoreException {
		Item goAnnotation = createItem(annotationClassName);
		goAnnotation.setReference("subject", productIdentifier);
		goAnnotation.setReference("ontologyTerm", termIdentifier);

		if (!StringUtils.isEmpty(qualifier)) {
			goAnnotation.setAttribute("qualifier", qualifier);
		}
		if (!StringUtils.isEmpty(annotationExtension)) {
			goAnnotation.setAttribute("annotationExtension", annotationExtension);
		}

		goAnnotation.addToCollection("dataSets", getDataset(dataSource, dataSourceCode));
		if ("gene".equals(productType)) {
			addProductCollection(productIdentifier, goAnnotation.getIdentifier());
		}
		Integer storedAnnotationId = store(goAnnotation);
		return storedAnnotationId;
	}

	private void addProductCollection(String productIdentifier, String goAnnotationIdentifier) {
		Integer storedProductId = storedProductIds.get(productIdentifier);
		List<String> annotationIds = productCollectionsMap.get(storedProductId);
		if (annotationIds == null) {
			annotationIds = new ArrayList<String>();
			productCollectionsMap.put(storedProductId, annotationIds);
		}
		annotationIds.add(goAnnotationIdentifier);
	}

	/**
	 * Given the 'with' text from a gene_association entry parse for recognised identifier
	 * types and create Gene or Protein items accordingly.
	 *
	 * @param withText string from the gene_association entry
	 * @param organism organism to reference
	 * @param dataSource the name of goa file source
	 * @param dataSourceCode short code to describe data source
	 * @throws ObjectStoreException if problem when storing
	 * @return a list of Items
	 */
	protected List<String> createWithObjects(String withText, Item organism,
			String dataSource, String dataSourceCode) throws ObjectStoreException {

		List<String> withProductList = new ArrayList<String>();
		try {
			String[] elements = withText.split("[; |,]");
			for (int i = 0; i < elements.length; i++) {
				String entry = elements[i].trim();
				// rely on the format being type:identifier
				if (entry.indexOf(':') > 0) {
					String prefix = entry.substring(0, entry.indexOf(':'));
					String value = entry.substring(entry.indexOf(':') + 1);

					if (WITH_TYPES.containsKey(prefix) && StringUtils.isNotEmpty(value)) {
						String className = WITH_TYPES.get(prefix);
						String productIdentifier = null;

						// if a UniProt protein it may be from a different organism
						// also FlyBase may be from a different Drosophila species
						if ("UniProt".equals(prefix)) {
							productIdentifier = newProduct(value, className,
									organism, dataSource, dataSourceCode,
									false, null);
						} else if ("FB".equals(prefix)) {
							// if organism is D. melanogaster then create with gene
							// TODO could still be wrong as the FBgn could be a different species
							if ("7227".equals(organism.getAttribute("taxonId").getValue())) {
								productIdentifier = newProduct(value, className, organism,
										dataSource, dataSourceCode, true, "primaryIdentifier");
							}
						} else {
							productIdentifier = newProduct(value, className, organism,
									dataSource, dataSourceCode, true, "primaryIdentifier");
						}
						if (productIdentifier != null) {
							withProductList.add(productIdentifier);
						}
					} else {
						LOG.debug("createWithObjects skipping a withType prefix:" + prefix);
					}
				}
			}
		} catch (RuntimeException e) {
			LOG.error("createWithObjects broke with: " + withText);
			throw e;
		}
		return withProductList;
	}

	private String newProduct(String identifier, String type, Item organism,
			String dataSource, String dataSourceCode, boolean createOrganism,
			String field) throws ObjectStoreException {
		String idField = field;
		String accession = identifier;
		String clsName = null;
		// find gene attribute first to see if organism should be part of key
		if ("gene".equalsIgnoreCase(type)) {
			clsName = "Gene";
			String taxonId = organism.getAttribute("taxonId").getValue();
		} else if ("protein".equalsIgnoreCase(type)) {
			// TODO use values in config
			clsName = "Protein";
			idField = "primaryAccession";
		} else {
			String typeCls = TypeUtil.javaiseClassName(type);

			if (getModel().getClassDescriptorByName(typeCls) != null) {
				Class<?> cls = getModel().getClassDescriptorByName(typeCls).getType();
				if (BioEntity.class.isAssignableFrom(cls)) {
					clsName = typeCls;
				}
			}
			if (clsName == null) {
				throw new IllegalArgumentException("Unrecognised annotation type '" + type + "'");
			}
		}

		boolean includeOrganism = false;

		String key = makeProductKey(accession, type, organism, includeOrganism);

		if (productMap.containsKey(key)) {
			return productMap.get(key);
		}

		// if a Dmel gene we need to use FlyBaseIdResolver to find a current id
		Item product = createItem(clsName);
		if (organism != null && createOrganism) {
			product.setReference("organism", organism.getIdentifier());
		}
		product.setAttribute(idField, accession);

		String dataSetIdentifier = getDataset(dataSource, dataSourceCode);
		product.addToCollection("dataSets", dataSetIdentifier);

		Integer storedProductId = store(product);
		storedProductIds.put(product.getIdentifier(), storedProductId);
		productMap.put(key, product.getIdentifier());
		return product.getIdentifier();
	}

	private String makeProductKey(String identifier, String type, Item organism,
			boolean createOrganism) {
		if (type == null) {
			throw new IllegalArgumentException("No type provided when creating " + organism
					+ ": " + identifier);
		} else if (identifier == null) {
			throw new IllegalArgumentException("No identifier provided when creating "
					+ organism + ": " + type);
		}

		return identifier + type.toLowerCase() + ((createOrganism)
				? organism.getIdentifier() : "");
	}


	private String resolveTerm(String identifier) {
		String goId = identifier;
		return goId;
	}


	private String newGoTerm(String identifier, String dataSource,
			String dataSourceCode) throws ObjectStoreException {
		if (identifier == null) {
			return null;
		}

		String goTermIdentifier = goTerms.get(identifier);
		if (goTermIdentifier == null) {
			Item item = createItem(termClassName);
			item.setAttribute("identifier", identifier);
			item.addToCollection("dataSets", getDataset(dataSource, dataSourceCode));
			store(item);

			goTermIdentifier = item.getIdentifier();
			goTerms.put(identifier, goTermIdentifier);
		}
		return goTermIdentifier;
	}

	private void storeEvidenceCode(String code, String annotType, String withText) throws ObjectStoreException {
		/*if (evidenceCodes.get(code) == null) {
            Item item = createItem("GOEvidenceCode");
            item.setAttribute("code", code);
            evidenceCodes.put(code, item.getIdentifier());
            store(item);
        }*/
		//, String annotType
		String combinationcode = "";
		if(withText == null || withText.isEmpty()) {
			combinationcode = code+":"+annotType; 
		}else {
			combinationcode = code+":"+annotType+":"+withText; 
		}


		if (evidenceCodes.get(combinationcode) == null) {
			Item item = createItem("GOEvidenceCode");
			item.setAttribute("code", code);
			if(!annotType.isEmpty()) {item.setAttribute("annotType", annotType); }
			if(withText != null && !withText.isEmpty()) { item.setAttribute("withText", withText); } 
			store(item);
			evidenceCodes.put(combinationcode, item.getIdentifier()); 
			System.out.println("Code is :  "+ combinationcode);
		}
	}

	private String getDataSourceCodeName(String sourceCode) {
		String title = sourceCode;

		// re-write some codes to better data source names
		if ("UniProtKB".equalsIgnoreCase(sourceCode)) {
			title = "UniProt";
		} else if ("FB".equalsIgnoreCase(sourceCode)) {
			title = "FlyBase";
		} else if ("WB".equalsIgnoreCase(sourceCode)) {
			title = "WormBase";
		} else if ("SP".equalsIgnoreCase(sourceCode)) {
			title = "UniProt";
		} else if (sourceCode.startsWith("GeneDB")) {
			title = "GeneDB";
		} else if ("SANGER".equalsIgnoreCase(sourceCode)) {
			title = "GeneDB";
		} else if ("GOA".equalsIgnoreCase(sourceCode)) {
			title = "Gene Ontology";
		} else if ("PINC".equalsIgnoreCase(sourceCode)) {
			title = "Proteome Inc.";
		} else if ("Pfam".equalsIgnoreCase(sourceCode)) {
			title = "PFAM"; // to merge with interpro
		}
		return title;
	}

	private String getDataset(String dataSource, String code)
			throws ObjectStoreException {
		String dataSetIdentifier = dataSets.get(code);
		if (dataSetIdentifier == null) {
			String dataSourceName = getDataSourceCodeName(code);
			String title = "GO Annotation from " + dataSourceName;
			Item item = createItem("DataSet");
			item.setAttribute("name", title);
			item.setReference("dataSource", getDataSource(getDataSourceCodeName(dataSource)));
			dataSetIdentifier = item.getIdentifier();
			dataSets.put(code, dataSetIdentifier);
			store(item);
		}
		return dataSetIdentifier;
	}


	/**
	 * Return a DataSource item for the given title
	 * @param name the DataSource name
	 * @return the DataSource Item
	 */
	public String getDataSource(String name) {
		if (name == null) {
			return null;
		}
		String refId = dataSources.get(name);
		if (refId == null) {
			Item dataSource = createItem("DataSource");
			dataSource.setAttribute("name", name);
			try {
				store(dataSource);
			} catch (ObjectStoreException e) {
				throw new RuntimeException("failed to store DataSource with name: " + name, e);
			}
			refId = dataSource.getIdentifier();
			dataSources.put(name, refId);
		}
		return refId;
	}


	private String newPublication(String pubMedId, String sgdId) throws ObjectStoreException {

		String pubRefId = null;

		if (pubMedId != null && sgdId !=null) { //lists both PMID and SGD_REF; it is assumed there will be one publication per line
			pubRefId = publications.get(pubMedId);
			if (pubRefId == null) {
				Item item = createItem("Publication");
				item.setAttribute("pubMedId", pubMedId);
				item.setAttribute("pubXrefId", sgdId);
				pubRefId = item.getIdentifier();
				publications.put(pubMedId, pubRefId);
				store(item);
			}
		} else if (pubMedId == null && sgdId !=null) {  //lists only GO_REF
			pubRefId = publications.get(sgdId);
			if (pubRefId == null) {
				Item item = createItem("Publication");
				item.setAttribute("pubXrefId", sgdId);
				pubRefId = item.getIdentifier();
				publications.put(sgdId,pubRefId);
				store(item);
			}
		}else if (pubMedId != null && sgdId == null) {  //lists only PMID
			pubRefId = publications.get(pubMedId);
			if (pubRefId == null) {
				Item item = createItem("Publication");
				item.setAttribute("pubMedId", pubMedId);
				pubRefId = item.getIdentifier();
				publications.put(pubMedId, pubRefId);
				store(item);
			}			          	
		}

		return pubRefId;
	}

	private String createDbReference(String value)
			throws ObjectStoreException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String dataSource = null;
		if (!dbRefs.contains(value)) {
			Item item = createItem("DatabaseReference");
			// FB:FBrf0055969
			if (value.contains(":")) {
				String[] bits = value.split(":");
				if (bits.length == 2) {
					String db = bits[0];
					dataSource = getDataSourceCodeName(db);
					value = bits[1];
				}
			}
			item.setAttribute("identifier", value);
			if (StringUtils.isNotEmpty(dataSource)) {
				item.setReference("source", getDataSource(dataSource));
			}
			dbRefs.add(value);
			store(item);
			return item.getIdentifier();
		}
		return null;
	}

	private Item newOrganism(String taxonId) throws ObjectStoreException {
		Item item = organisms.get(taxonId);
		if (item == null) {
			item = createItem("Organism");
			item.setAttribute("taxonId", taxonId);
			organisms.put(taxonId, item);
			store(item);
		}
		return item;
	}

	private String parseTaxonId(String input) {
		if ("taxon:".equals(input)) {
			throw new IllegalArgumentException("Invalid taxon id read: " + input);
		}
		String taxonId = input.split(":")[1];
		if (taxonId.contains("|")) {
			taxonId = taxonId.split("\\|")[0];
		}
		return taxonId;
	}

	private class Evidence
	{
		private List<String> publicationRefIds = new ArrayList<String>();
		private String evidenceCode = null;
		private Integer storedAnnotationId = null;
		private String withText = null;
		private Item organism = null;
		private String dataSourceCode = null;
		private String dataSource = null;

		protected Evidence(String evidenceCode, String publicationRefId, String withText,
				Item organism, String dataset, String datasource) {
			this.evidenceCode = evidenceCode;
			this.withText = withText;
			this.organism = organism;
			this.dataSourceCode = dataset;
			this.dataSource = datasource;
			addPublicationRefId(publicationRefId);
		}

		protected void addPublicationRefId(String publicationRefId) {
			if (publicationRefId != null) {
				publicationRefIds.add(publicationRefId);
			}
		}

		protected List<String> getPublications() {

			return publicationRefIds;
		}

		protected String getEvidenceCode() {
			return evidenceCode;
		}

		@SuppressWarnings("unused")
		protected String getWithText() {
			return withText;
		}

		@SuppressWarnings("unused")
		protected String getDataset() {
			return dataSourceCode;
		}

		@SuppressWarnings("unused")
		protected String getDatasource() {
			return dataSource;
		}

		@SuppressWarnings("unused")
		protected Item getOrganism() {
			return organism;
		}


		/**
		 * @return the storedAnnotationId
		 */
		protected Integer getStoredAnnotationId() {
			return storedAnnotationId;
		}

		/**
		 * @param storedAnnotationId the storedAnnotationId to set
		 */
		protected void setStoredAnnotationId(Integer storedAnnotationId) {
			this.storedAnnotationId = storedAnnotationId;
		}
	}


	/**
	 * Identify a GoTerm/geneProduct pair with qualifier
	 * used to also use evidence code
	 */
	private class GoTermToGene
	{
		private String productId;
		private String goId;
		private String qualifier;
		private String withText;
		private String annotationExtension;
		private String pub;
		private String pubxref;

		/**
		 * Constructor
		 *
		 * @param productId gene/protein identifier
		 * @param goId      GO term id
		 * @param qualifier qualifier
		 */
		GoTermToGene(String productId, String goId, String qualifier, String withText, String annotationExtension, String pub, String pubxref) {
			this.productId = productId;
			this.goId = goId;
			this.qualifier = qualifier;
			this.withText = withText;
			this.annotationExtension = annotationExtension;
			this.pub = pub;
			this.pubxref = pubxref;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof GoTermToGene) {
				GoTermToGene go = (GoTermToGene) o;
				if(annotationExtension == null) {
					return productId.equals(go.productId)
							&& goId.equals(go.goId)
							&& qualifier.equals(go.qualifier)
							&& withText.equals(go.withText)
							&& pub.equals(go.pub)
							&& pubxref.equals(go.pubxref);
				}else{
					System.out.println("in GoTermToGene...." + annotationExtension);
					return productId.equals(go.productId)
							&& goId.equals(go.goId)
							&& qualifier.equals(go.qualifier)
							&& withText.equals(go.withText)
							&& pub.equals(go.pub)
							&& pubxref.equals(go.pubxref)
							&& annotationExtension.equals(go.annotationExtension);
				}
			}
			return false;
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataSetTitle(int taxonId) {
		return DATASET_TITLE;
	}

}
