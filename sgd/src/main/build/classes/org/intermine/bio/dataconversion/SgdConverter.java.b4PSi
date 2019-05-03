package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2010 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.sql.Connection;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils; 
//import org.biojava.bio.program.homologene.OrthoPairSet.Iterator;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;

/**
 * Converts results sets into intermine objects
 * 
 * @author Julie Sullivan
 */
public class SgdConverter extends BioDBConverter {

	// private static final Logger LOG = Logger.getLogger(SgdConverter.class);
	private static final String DATASET_TITLE = "SGD data set";
	private static final String DATA_SOURCE_NAME = "SGD";
	private Map<String, String> chromosomes = new HashMap();
	private Map<String, String> plasmids = new HashMap();
	private Map<String, String> sequences = new HashMap();
	private Map<String, Item> interactions = new HashMap();
	private Map<MultiKey, Item> interactionsnew = new HashMap<MultiKey, Item>();
	private Map<String, String> literatureTopics = new HashMap();
	private Map<String, Item> genes = new HashMap();
	private Map<String, Item> genesName = new HashMap();
	private Map<String, String> genesAliases = new HashMap();
	private Map<String, String> synonyms = new HashMap();
	private Map<String, Item> publications = new HashMap();
	private Map<String, Item> interactiontype = new HashMap();
	private Map<String, Item> interactiondetail = new HashMap();
	private Map<String, Item> experimenttype = new HashMap();
	private Map<String, Item> interactiondetectionmethods = new HashMap();
	private Map<String, Item> pathways = new HashMap();
	private Map<String, Item> phenotypes = new HashMap();
	private Map<String, String> datasources = new HashMap();
	private static final String TAXON_ID = "4932";
	private Item organism;
	private Map<String, String> featureMap = new HashMap();
	private static final boolean TEST_LOCAL = true;


	private static final SgdProcessor PROCESSOR = new SgdProcessor();

	/**
	 * Construct a new SgdConverter.
	 * 
	 * @param database
	 *            the database to read from
	 * @param model
	 *            the Model used by the object store we will write to with the
	 *            ItemWriter
	 * @param writer
	 *            an ItemWriter used to handle Items created
	 * @throws ObjectStoreException
	 *             if organism can't be stored
	 */
	public SgdConverter(Database database, Model model, ItemWriter writer)
			throws ObjectStoreException {
		super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
		organism = createItem("Organism");
		organism.setAttribute("taxonId", TAXON_ID);
		organism.setAttribute("genus", "Saccharomyces");
		organism.setAttribute("species", "cerevisiae");
		organism.setAttribute("name", "Saccharomyces cerevisiae");
		organism.setAttribute("shortName", "S. cerevisiae");
		store(organism);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() throws Exception {

		// a database has been initialized from properties starting with db.sgd
		Connection connection = getDatabase().getConnection();

		processChromosomeSequences(connection);
		processGenes(connection);
		processAliases(connection);
		processCrossReferences(connection);
		processGeneLocations(connection);
		processChrLocations(connection);
		processGeneChildrenLocations(connection);
		processProteins(connection);
		processAllPubs(connection);             //get all publications and their topics loaded								
		processPubsWithFeatures(connection);    //for chromosomal features load pubmed and topics	
		processParalogs(connection);

		if(!TEST_LOCAL) {
			processPhenotypes(connection);
			processPubsForPhenotypes(connection);
			storePhenotypes();

			processPathways(connection);
			storePathways();

			processInteractions(connection);
			storeInteractionTypes();
			storeInteractionExperiments();
			storeInteractions();
		}
		storePublications();
		storeGenes();

	}

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */

	private void processGenes(Connection connection) throws SQLException,
	ObjectStoreException {

		ResultSet res = PROCESSOR.getChromosomalFeatureResults(connection);
		System.out.println("Processing Genes...");

		while (res.next()) {

			String featureNo = res.getString("feature_no");
			if (genes.get(featureNo) == null) {
				// ~~~ gene ~~~
				String primaryIdentifier = res.getString("dbxref_id");
				String secondaryIdentifier = res.getString("feature_name");
				String symbol = res.getString("gene_name");
				String feature_type = res.getString("feature_type");
				String name = res.getString("name_description");
				String headline = res.getString("headline");
				String description = res.getString("description");
				String qualifier = res.getString("qualifier");
				String feat_attribute = res.getString("feat_attribute");
				String status = res.getString("status");
                //System.out.println("feature type is.." + feature_type);
				Item item = null;
				if (feature_type.equalsIgnoreCase("ORF")) {
					item = createItem("ORF");
				} else if (feature_type.equalsIgnoreCase("pseudogene")) {
					item = createItem("Pseudogene");
				} else if (feature_type
						.equalsIgnoreCase("transposable_element_gene")) {
					item = createItem("TransposableElementGene");
				} else if (feature_type
						.equalsIgnoreCase("not physically mapped")) {
					item = createItem("NotPhysicallyMapped");
				} else if (feature_type
						.equalsIgnoreCase("long_terminal_repeat")) {
					item = createItem("LongTerminalRepeat");
				} else if (feature_type.equalsIgnoreCase("ARS")) {
					item = createItem("ARS");
				} else if (feature_type.equalsIgnoreCase("tRNA_gene")) {
					item = createItem("TRNAGene");
				} else if (feature_type.equalsIgnoreCase("snoRNA_gene")) {
					item = createItem("SnoRNAGene");
				} else if (feature_type
						.equalsIgnoreCase("not in systematic sequence of S288C")) {
					item = createItem("NotInSystematicSequenceOfS288C");
				} else if (feature_type.equalsIgnoreCase("LTR_retrotransposon")) {
					item = createItem("Retrotransposon");
				} else if (feature_type
						.equalsIgnoreCase("X_element_combinatorial_repeats")) {
					item = createItem("XElementCombinatorialRepeat");
				} else if (feature_type
						.equalsIgnoreCase("X_element")) {
					item = createItem("XElement");
				} else if (feature_type.equalsIgnoreCase("telomere")) {
					item = createItem("Telomere");
				} else if (feature_type.equalsIgnoreCase("telomeric_repeat")) {
					item = createItem("TelomericRepeat");
				} else if (feature_type.equalsIgnoreCase("rRNA_gene")) {
					item = createItem("RRNAGene");
				} else if (feature_type.equalsIgnoreCase("Y_prime_element")) {
					item = createItem("YPrimeElement");
				} else if (feature_type.equalsIgnoreCase("centromere")) {
					item = createItem("Centromere");
				} else if (feature_type.equalsIgnoreCase("ncRNA_gene")) {
					item = createItem("NcRNAGene");
				} else if (feature_type.equalsIgnoreCase("snRNA_gene")) {
					item = createItem("SnRNAGene");
				}else if (feature_type.equalsIgnoreCase("blocked_reading_frame")) {
					item = createItem("BlockedReadingFrame");
				}else if (feature_type.equalsIgnoreCase("origin_of_replication")) {
					item = createItem("OriginOfReplication");
				}else if (feature_type.equalsIgnoreCase("matrix_attachment_site")) {
					item = createItem("MatrixAttachmentSite");
				}else if (feature_type.equalsIgnoreCase("telomerase_RNA_gene")) {
					item = createItem("TelomeraseRNAGene");
				}

				// set for all types, so you can use LSF to query for these
				// different type of objects in a template.
				item.setAttribute("featureType", feature_type);

				item.setAttribute("featureType", feature_type);
				item.setAttribute("primaryIdentifier", primaryIdentifier);
				if (StringUtils.isNotEmpty(name)) {
					item.setAttribute("name", name);
				}

				item.setAttribute("secondaryIdentifier", secondaryIdentifier);
				item.setReference("organism", organism);

				if (StringUtils.isNotEmpty(symbol)) {
					item.setAttribute("symbol", symbol);
				}
				if (StringUtils.isNotEmpty(description)) {
					item.setAttribute("description", description);
				}
				if (StringUtils.isNotEmpty(headline)) {
					item.setAttribute("briefDescription", headline);
				}
				if (qualifier != null) {
					item.setAttribute("qualifier", qualifier);
					if (StringUtils.isNotEmpty(qualifier)) {
						item.setAttribute("qualifier", qualifier);
					}
				}
				if (feat_attribute != null) {
					item.setAttribute("featAttribute", feat_attribute);
					if (StringUtils.isNotEmpty(feat_attribute)) {
						item.setAttribute("featAttribute", feat_attribute);
					}
				}
				if (status != null) {
					item.setAttribute("status", status);
					if (StringUtils.isNotEmpty(status)) {
						item.setAttribute("status", status);
					}
				}

				String refId = item.getIdentifier();
				genes.put(featureNo, item);
				genesName.put(secondaryIdentifier, item);

				// ~~~ synonyms ~~~
				getSynonym(refId, "symbol", symbol);
				if (symbol != null) {
					if (!symbol.equalsIgnoreCase(secondaryIdentifier)) {
						getSynonym(refId, "identifier", secondaryIdentifier);// RPM1
						// problem
					}
				}
				getSynonym(refId, "identifier", primaryIdentifier);
			}
		}
		System.out.println("size of genes:  " + genes.size());
	}
	

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */

	private void processParalogs(Connection connection) throws SQLException,
	ObjectStoreException {

		ResultSet res = PROCESSOR.getParalogs(connection); // ordered by
		// featureNo
		System.out.println("Processing Paralog pairs...");

		while (res.next()) {

			String parentFeatureNo = res.getString("parent_feature_no");
			String childFeatureNo = res.getString("child_feature_no");
			String refNo = res.getString("reference_no");		
			String source="";
			if(refNo.equalsIgnoreCase("50997")){ //hack for PMID 16169922
				source= "YGOB";
			}else{
				source = "SGD";
			}
			Item pmid = getExistingPub(refNo);
			Item parentGene = genes.get(parentFeatureNo);
			Item childGene = genes.get(childFeatureNo);

			if(parentGene!= null && childGene != null) {	
				processHomologues(parentGene.getIdentifier(), childGene.getIdentifier(), pmid.getIdentifier(), source);
				processHomologues(childGene.getIdentifier(), parentGene.getIdentifier(), pmid.getIdentifier(), source);

			}

		}
	}

	private void processHomologues(String gene1, String gene2, String pmid, String source) throws ObjectStoreException {


		if (gene1 == null || gene2 == null) {
			return;
		}
		
		Item homologue = createItem("Homologue");
		homologue.setReference("gene", gene1);
		homologue.setReference("homologue", gene2);
		homologue.setAttribute("type", "paralogue"); 
		homologue.setAttribute("source", source);
	    homologue.setReference("publication", pmid);
		store(homologue);
	}


	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */

	private void processAliases(Connection connection) throws SQLException,
	ObjectStoreException {

		ResultSet res = PROCESSOR.getAliases(connection); // ordered by
		// featureNo
		System.out.println("Processing ALiases...");
		while (res.next()) {

			String geneFeatureNo = res.getString("gene_feature_no");
			String alias_type = res.getString("alias_type");
			String alias_name = res.getString("alias_name");

			Item item = genes.get(geneFeatureNo);
			if (item != null) {

				// adding sgd_aliases as synonyms..
				getSynonym(item.getIdentifier(), alias_type, alias_name);

				String name = genesAliases.get(geneFeatureNo);
				if (name == null) {
					genesAliases.put(geneFeatureNo, alias_name);
				} else {
					String newname = name + " " + alias_name;
					genesAliases.put(geneFeatureNo, newname);
				}
			}

		}

		// Add the concatenated string as alias name
		Set<Map.Entry<String, String>> set = genesAliases.entrySet();
		java.util.Iterator<Map.Entry<String, String>> it = set.iterator();

		while (it.hasNext()) {

			Map.Entry<String, String> anEntry = it.next();
			String geneFeatureNo = anEntry.getKey();
			String alias = anEntry.getValue();
			Item item = genes.get(geneFeatureNo);
			item.setAttribute("sgdAlias", alias);
		}

	}


	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */
	private void processPathways(Connection connection)
			throws SQLException, ObjectStoreException {

		ResultSet res = PROCESSOR.getPathways(connection); // ordered by featureNo

		System.out.println("Processing Pathways...");

		while (res.next()) {

			String geneFeatureNo = res.getString("feature_no");
			String dbxref_name = res.getString("dbxref_name"); //pathway name
			String dbxref_id = res.getString("dbxref_id"); //pathway identifier i.e. short name
			Item item = genes.get(geneFeatureNo);

			if (item != null) {
				getPathway(item.getIdentifier(), dbxref_id, dbxref_name);
			}
		}

	}


	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */
	private void processCrossReferences(Connection connection)
			throws SQLException, ObjectStoreException {

		ResultSet res = PROCESSOR.getCrossReferences(connection); // ordered by
		// featureNo

		System.out.println("Processing DbXRefs...");

		while (res.next()) {

			String geneFeatureNo = res.getString("feature_no");
			String dbx_source = res.getString("source");
			String dbxref_id = res.getString("dbxref_id");

			Item item = genes.get(geneFeatureNo);

			if (item != null) {
				getCrossReference(item.getIdentifier(), dbxref_id, dbx_source);
			}
		}

	}

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 * @throws Exception
	 */

	private void processGeneLocations(Connection connection)
			throws SQLException, ObjectStoreException, Exception {

		ResultSet res = PROCESSOR.getChromosomalFeatureLocationResults(connection);
		System.out.println("Processing GeneLocations...");
		while (res.next()) {
			String featureNo = res.getString("feature_no");
			String geneFeatureNo = res.getString("gene_feature_no");
			String featureType = res.getString("feature_type");
			String seq_length = res.getString("seq_length");
			String featureName = res.getString("identifier");
			String strand = res.getString("strand");

			String newstrand = "";
			if (strand.equals("+")) {
				newstrand = "1";
			} else if (strand.equals("-")) {
				newstrand = "-1";
			} else if (strand.equals("0")) {
				newstrand = "0";
			}

			String fixed_chromosome_no = getFixedChrName(featureName);

			//if (featureName.equalsIgnoreCase("17")) {
			//featureName = "chrMito";
			//}else if(!featureName.equalsIgnoreCase("2-micron")){
			//featureName = "chr"+featureName;
			//}

			Item item = genes.get(geneFeatureNo);

			// ~~~ chromosome OR plasmid ~~~
			String refId = null;
			if (featureType.equalsIgnoreCase("plasmid")) {
				refId = getPlasmid(fixed_chromosome_no);// featureNo,
				item.setReference("plasmid", refId);
			} else if (featureType.equalsIgnoreCase("chromosome")) {
				refId = getChromosome(fixed_chromosome_no); // featureNo,
				item.setReference("chromosome", refId);
			}

			// ~~~ location ~~~
			String locationRefId = getLocation(item, refId, res
					.getString("min_coord"), res.getString("max_coord"),
					newstrand); // res.getString("strand")

			if (featureType.equalsIgnoreCase("plasmid")) {
				item.setReference("plasmidLocation", locationRefId);
			} else if (featureType.equalsIgnoreCase("chromosome")) {
				item.setReference("chromosomeLocation", locationRefId);
			}
			// ~~ add sequence
			String seqRefId = getSequence(geneFeatureNo, res
					.getString("residues"), seq_length);
			item.setReference("sequence", seqRefId);
			item.setAttribute("length", seq_length);

		}
		res.close();
	}

	/**
	 * To add a chromosome number to the NISS and NPM features that do have a
	 * chromosome number as parent
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */
	private void processChrLocations(Connection connection)
			throws SQLException, ObjectStoreException, Exception {

		ResultSet res = PROCESSOR.getChromosomeLocationResults(connection);
		System.out.println("Processing ChrLocations...");
		while (res.next()) {
			String featureNo = res.getString("feature_no");
			String geneFeatureNo = res.getString("gene_feature_no");
			String featureType = res.getString("feature_type");
			String featureName = res.getString("identifier");

			String fixed_chromosome_no = getFixedChrName(featureName);

			//if (featureName.equalsIgnoreCase("17")) {
			//	featureName = "chrMito";
			//}else if(!featureName.equalsIgnoreCase("2-micron")){
			//	featureName = "chr"+featureName;
			//}

			Item item = genes.get(geneFeatureNo);

			// ~~~ chromosome OR plasmid ~~~
			String refId = null;
			if (featureType.equalsIgnoreCase("plasmid")) {
				refId = getPlasmid(fixed_chromosome_no);
				item.setReference("plasmid", refId);
			} else if (featureType.equalsIgnoreCase("chromosome")) {
				refId = getChromosome(fixed_chromosome_no);
				item.setReference("chromosome", refId);
			}

			// ~~~ location ~~~
			String locationRefId = getLocation(item, refId, "1", "1", "0"); // was
			// -
			// 0,0,n/a
			// or
			// should
			// it
			// be
			// some
			// number..1,1,0

			if (featureType.equalsIgnoreCase("plasmid")) {
				item.setReference("plasmidLocation", locationRefId);
			} else if (featureType.equalsIgnoreCase("chromosome")) {
				item.setReference("chromosomeLocation", locationRefId);
			}

		}
		res.close();
	}

	private void processGeneChildrenLocations(Connection connection)
			throws SQLException, ObjectStoreException, Exception {

		ResultSet res = PROCESSOR.getChildrenFeatureLocationResults(connection);
		System.out.println("Processing GeneChildrenLocations...");
		while (res.next()) {

			String geneFeatureNo = res.getString("parent_id");
			String parentFeatureType = res.getString("parent_type");

			String geneChildFeatureNo = res.getString("child_id");
			String childFeatureType = res.getString("child_type");

			String chromosome_no = res.getString("feature_name"); // root chr.
			// number
			String secondaryIdentifier = res.getString("child_identifier"); // child
			String primaryIdentifier = res.getString("child_dbxrefid"); // SXX

			String maxcoord = res.getString("max_coord");
			String mincoord = res.getString("min_coord");
			String strand = res.getString("strand");

			String seq = res.getString("residues");
			String seqLen = res.getString("seq_length");
			String child_status = res.getString("child_status");

			String newstrand = "";
			if (strand.equals("+")) {
				newstrand = "1";
			} else if (strand.equals("-")) {
				newstrand = "-1";
			} else if (strand.equals("0")) {
				newstrand = "0";
			}

			String fixed_chromosome_no = getFixedChrName(chromosome_no);

			//if (chromosome_no.equalsIgnoreCase("17")) {
			//chromosome_no = "chrMito";
			//}else if(!chromosome_no .equalsIgnoreCase("2-micron")){
			//chromosome_no = "chr"+chromosome_no;
			//}

			// figure out why duplicates in the SQL..???..
			if (featureMap.get(geneChildFeatureNo) == null) {
				featureMap.put(geneChildFeatureNo, geneFeatureNo);
			} else {
				continue;
			}

			Item parent = genes.get(geneFeatureNo);

			//System.out.println("child feature type...."+ childFeatureType);
			// create the child Item
			Item childItem = getChildItem(childFeatureType);

			childItem.setAttribute("primaryIdentifier", primaryIdentifier);
			// childItem.setAttribute("secondaryIdentifier",
			// secondaryIdentifier);
			childItem.setReference("organism", organism);
			childItem.setAttribute("featureType", childFeatureType);
			childItem.setAttribute("status", child_status);

			if (childFeatureType.equalsIgnoreCase("intron")) {
				childItem.addToCollection("genes", parent.getIdentifier());
			} else {
				String refname = getReferenceName(childFeatureType,
						parentFeatureType);
				childItem.setReference(refname, parent.getIdentifier());
			}

			// ~~ add sequence
			String seqRefId = getSequence(geneChildFeatureNo, seq, seqLen);
			childItem.setReference("sequence", seqRefId);

			// ~~~ chromosome and location ~~~
			String refId = null;
			if (fixed_chromosome_no.equalsIgnoreCase("2-micron")) {
				refId = getPlasmid(fixed_chromosome_no);
				childItem.setReference("plasmid", refId);
				String locationRefId = getLocation(childItem, refId, mincoord,
						maxcoord, newstrand);
				childItem.setReference("plasmidLocation", locationRefId);
			} else {
				refId = getChromosome(fixed_chromosome_no);
				childItem.setReference("chromosome", refId);
				String locationRefId = getLocation(childItem, refId, mincoord,
						maxcoord, newstrand);
				childItem.setReference("chromosomeLocation", locationRefId);
			}

			try {
				store(childItem);
			} catch (ObjectStoreException e) {
				e.printStackTrace();
				throw new ObjectStoreException(e);
			}

			// ~~~ store these last ~~~
			String childId = childItem.getIdentifier();
			getSynonym(childId, "identifier", primaryIdentifier);
			// getSynonym(childId, "identifier", secondaryIdentifier); - eurie
			// doesn't want this to be searchable

		}
		res.close();

	}

	private String getReferenceName(String type, String ptype)
			throws ObjectStoreException {

		String name = "";

      if (type.equalsIgnoreCase("ARS_consensus_sequence") && ptype.equalsIgnoreCase("ARS")) {
			name = "ars";
		}else if (type.equalsIgnoreCase("CDS") && ptype.equalsIgnoreCase("blocked_reading_frame")) {
			name = "blockedreadingframe";
		}else if (type.equalsIgnoreCase("CDS") && ptype.equalsIgnoreCase("ORF")) {
			name = "orf";
		} else if (type.equalsIgnoreCase("CDS") && ptype.equalsIgnoreCase("pseudogene")) {
			name = "pseudogene";
		} else if (type.equalsIgnoreCase("CDS") && ptype.equalsIgnoreCase("transposable_element_gene")) {
			name = "transposableelementgene";
		}else if (type.equalsIgnoreCase("centromere_DNA_Element_I")) {
			name = "centromere";
		} else if (type.equalsIgnoreCase("centromere_DNA_Element_II")) {
			name = "centromere";
		} else if (type.equalsIgnoreCase("centromere_DNA_Element_III")) {
			name = "centromere";
		} else if (type.equalsIgnoreCase("external_transcribed_spacer_region")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("five_prime_UTR_intron")) {
			name = "orf";
		}else if (type.equalsIgnoreCase("intein_encoding_region")) {
			name = "orf";
		}else if (type.equalsIgnoreCase("internal_transcribed_spacer_region")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("intron") && ptype.equalsIgnoreCase("ORF")) {
			name = "orf";
		}else if (type.equalsIgnoreCase("intron") && ptype.equalsIgnoreCase("rRNA_gene")) {
			name = "rrna_gene";
		}else if (type.equalsIgnoreCase("intron") && ptype.equalsIgnoreCase("snoRNA_gene")) {
			name = "snorna_gene";
		}else if (type.equalsIgnoreCase("intron") && ptype.equalsIgnoreCase("tRNA_gene")) {
			name = "trna_gene";
		} else if (type.equalsIgnoreCase("non_transcribed_region")) {
			name = "ncrna_gene";
		}else if (type.equalsIgnoreCase("noncoding_exon") && ptype.equalsIgnoreCase("ncRNA_gene")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("noncoding_exon") && ptype.equalsIgnoreCase("rRNA_gene")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("noncoding_exon") && ptype.equalsIgnoreCase("snoRNA_gene")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("noncoding_exon") && ptype.equalsIgnoreCase("snRNA_gene")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("noncoding_exon") && ptype.equalsIgnoreCase("tRNA_gene")) {
			name = "ncrna_gene";
		} else if (type.equalsIgnoreCase("plus_1_translational_frameshift") && ptype.equalsIgnoreCase("ORF")) {
			name = "orf";
		} else if (type.equalsIgnoreCase("plus_1_translational_frameshift") && ptype.equalsIgnoreCase("pseudogene")) {
			name = "pseudogene";
		} else if (type.equalsIgnoreCase("plus_1_translational_frameshift") && ptype.equalsIgnoreCase("transposable_element_gene")) {
			name = "transposableelementgene";
		} else if (type.equalsIgnoreCase("telomeric_repeat") && ptype.equalsIgnoreCase("telomere")) {
			name = "telomere";
		}  else if (type.equalsIgnoreCase("X_element") && ptype.equalsIgnoreCase("telomere")) {
			name = "telomere";
		} else if (type.equalsIgnoreCase("X_element_combinatorial_repeat") && ptype.equalsIgnoreCase("telomere")) {
			name = "telomere";
		}   else if (type.equalsIgnoreCase("Y_prime_element") && ptype.equalsIgnoreCase("telomere")) {
			name = "telomere";
		}  else if (type.equalsIgnoreCase("noncoding_exon") && ptype.equalsIgnoreCase("telomerase_RNA_gene")) {
			name = "ncrna_gene";
		}

		return name;

	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */

	private String getCasedName(String name) throws Exception {

		String newname = name;

		StringBuffer sb = new StringBuffer();
		Matcher m = Pattern
				.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(
						name);
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase()
					+ m.group(2).toLowerCase());
		}
		newname = m.appendTail(sb) + "p".toString();

		return newname;

	}

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 * @throws Exception
	 */
	private void processProteins(Connection connection) throws SQLException,
	ObjectStoreException, Exception {

		ResultSet res = PROCESSOR.getProteinResults(connection);
		System.out.println("Processing Proteins...");
		while (res.next()) {

			String featureNo = res.getString("feature_no");
			String primaryIdentifier = res.getString("dbxref_id");
			String secondaryIdentifier = res.getString("feature_name");
			String symbol = res.getString("gene_name");
			String residues = res.getString("residues");
			String length = res.getString(6);
			String molwt = res.getString("molecular_weight");
			String pi = res.getString("pi");
			String fopScore = res.getString("fop_score");
			String gravyScore = res.getString("gravy_score");
			String aromaticityScore = res.getString("aromaticity_score");
			String cys = res.getString("cys");
			String gln = res.getString("gln");
			String glu = res.getString("glu");
			String gly = res.getString("gly");
			String his = res.getString("his");
			String ile = res.getString("ile");
			String leu = res.getString("leu");
			String lys = res.getString("lys");
			String met = res.getString("met");
			String phe = res.getString("phe");
			String pro = res.getString("pro");
			String ser = res.getString("ser");
			String thr = res.getString("thr");
			String trp = res.getString("trp");
			String tyr = res.getString("tyr");
			String val = res.getString("val");
			String ala = res.getString("ala");
			String arg = res.getString("arg");
			String asn = res.getString("asn");
			String asp = res.getString("asp");
			String ntermseq = res.getString("n_term_seq");
			String ctermseq = res.getString("c_term_seq");
			String cai = res.getString("cai");
			String codonBias = res.getString("codon_bias");

			Item item = genes.get(featureNo);

			// ~~~ sequence ~~~
			Item protein = createItem("Protein");
			protein.setAttribute("primaryIdentifier", primaryIdentifier);
			protein.setAttribute("secondaryIdentifier", secondaryIdentifier);
			protein.setAttribute("length", length);
			protein.setReference("organism", organism);

			if (symbol != null) {
				String modSymbol = getCasedName(symbol);
				protein.setAttribute("symbol", modSymbol);
			}

			if (molwt != null) {
				protein.setAttribute("molecularWeight", molwt);
			}
			if (pi != null) {
				protein.setAttribute("pI", pi);
			}
			if (fopScore != null) {
				protein.setAttribute("fopScore", fopScore);
			}
			if (gravyScore != null) {
				protein.setAttribute("gravyScore", gravyScore);
			}
			if (aromaticityScore != null) {
				protein.setAttribute("aromaticityScore", aromaticityScore);
			}
			if (cys != null) {
				protein.setAttribute("cys", cys);
			}
			if (gln != null) {
				protein.setAttribute("gln", gln);
			}
			if (glu != null) {
				protein.setAttribute("glu", glu);
			}
			if (gly != null) {
				protein.setAttribute("gly", gly);
			}
			if (his != null) {
				protein.setAttribute("his", his);
			}
			if (ile != null) {
				protein.setAttribute("ile", ile);
			}
			if (leu != null) {
				protein.setAttribute("leu", leu);
			}
			if (lys != null) {
				protein.setAttribute("lys", lys);
			}
			if (met != null) {
				protein.setAttribute("met", met);
			}
			if (phe != null) {
				protein.setAttribute("phe", phe);
			}
			if (pro != null) {
				protein.setAttribute("pro", pro);
			}
			if (ser != null) {
				protein.setAttribute("ser", ser);
			}
			if (thr != null) {
				protein.setAttribute("thr", thr);
			}
			if (trp != null) {
				protein.setAttribute("trp", trp);
			}
			if (val != null) {
				protein.setAttribute("val", val);
			}
			if (ala != null) {
				protein.setAttribute("ala", ala);
			}
			if (arg != null) {
				protein.setAttribute("arg", arg);
			}
			if (asn != null) {
				protein.setAttribute("asn", asn);
			}
			if (asp != null) {
				protein.setAttribute("asp", asp);
			}
			if (ntermseq != null) {
				protein.setAttribute("ntermseq", ntermseq);
			}
			if (ctermseq != null) {
				protein.setAttribute("ctermseq", ctermseq);
			}
			if (cai != null) {
				protein.setAttribute("cai", cai);
			}
			if (codonBias != null) {
				protein.setAttribute("codonBias", codonBias);
			}
			

			Item seq = createItem("Sequence");
			seq.setAttribute("residues", residues);
			seq.setAttribute("length", length);

			try {
				store(seq);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}

			protein.setReference("sequence", seq.getIdentifier());
			protein.addToCollection("genes", item.getIdentifier());

			try {
				store(protein);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}

		}


	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */

	private String getFixedChrName(String name) throws Exception {

		String newname = "";

		if (name.equalsIgnoreCase("17")) {
			newname = "chrmt";
		}else if(name.equalsIgnoreCase("2-micron")){
			newname = name;
		}else if(name.equalsIgnoreCase("1")){
			newname ="chrI";
		}else if(name.equalsIgnoreCase("2")){
			newname ="chrII";
		}else if(name.equalsIgnoreCase("3")){
			newname ="chrIII";
		}else if(name.equalsIgnoreCase("4")){
			newname ="chrIV";
		}else if(name.equalsIgnoreCase("5")){
			newname ="chrV";
		}else if(name.equalsIgnoreCase("6")){
			newname ="chrVI";
		}else if(name.equalsIgnoreCase("7")){
			newname ="chrVII";
		}else if(name.equalsIgnoreCase("8")){
			newname ="chrVIII";
		}else if(name.equalsIgnoreCase("9")){
			newname ="chrIX";
		}else if(name.equalsIgnoreCase("10")){
			newname ="chrX";
		}else if(name.equalsIgnoreCase("11")){
			newname ="chrXI";
		}else if(name.equalsIgnoreCase("12")){
			newname ="chrXII";
		}else if(name.equalsIgnoreCase("13")){
			newname ="chrXIII";
		}else if(name.equalsIgnoreCase("14")){
			newname ="chrXIV";
		}else if(name.equalsIgnoreCase("15")){
			newname ="chrXV";
		}else if(name.equalsIgnoreCase("16")){
			newname ="chrXVI";
		}



		//else {
		//newname = "chr"+name;
		//}

		return newname;

	}

	/**
	 * 
	 * @param childType
	 * @return
	 * @throws ObjectStoreException
	 */
	private Item getChildItem(String childType) throws ObjectStoreException {

		Item item = null;

		if (childType.equalsIgnoreCase("CDS")) {
			item = createItem("CDS");
		} else if (childType.equalsIgnoreCase("intron")) {
			item = createItem("Intron");
		} else if (childType.equalsIgnoreCase("five_prime_UTR_intron")) {
			item = createItem("FivePrimeUTRIntron");
		} else if (childType
				.equalsIgnoreCase("plus_1_translational_frameshift")) {
			item = createItem("Plus1TranslationalFrameshift");
		} else if (childType.equalsIgnoreCase("ARS consensus sequence")) {
			item = createItem("ARSConsensusSequence");
		} else if (childType.equalsIgnoreCase("binding_site")) {
			item = createItem("BindingSite");
		} else if (childType.equalsIgnoreCase("insertion")) {
			item = createItem("Insertion");
		} else if (childType.equalsIgnoreCase("repeat_region")) {
			item = createItem("RepeatRegion");
		} else if (childType.equalsIgnoreCase("noncoding_exon")) {
			item = createItem("NoncodingExon");
		} else if (childType
				.equalsIgnoreCase("external_transcribed_spacer_region")) {
			item = createItem("ExternalTranscribedSpacerRegion");
		} else if (childType
				.equalsIgnoreCase("internal_transcribed_spacer_region")) {
			item = createItem("InternalTranscribedSpacerRegion");
		} else if (childType.equalsIgnoreCase("non_transcribed_region")) {
			item = createItem("NonTranscribedRegion");
		} else if (childType.equalsIgnoreCase("centromere_DNA_Element_I")) {
			item = createItem("CentromereDNAElementI");
		} else if (childType.equalsIgnoreCase("centromere_DNA_Element_II")) {
			item = createItem("CentromereDNAElementII");
		} else if (childType.equalsIgnoreCase("centromere_DNA_Element_III")) {
			item = createItem("CentromereDNAElementIII");
		} else if (childType.equalsIgnoreCase("intein_encoding_region")) {
			item = createItem("InteinEncodingRegion");
		} else if (childType.equalsIgnoreCase("ARS_consensus_sequence")) {
			item = createItem("ARSConsensusSequence");
		}else if (childType.equalsIgnoreCase("Y_prime_element")) {
			item = createItem("YPrimeElement");
		}else if (childType.equalsIgnoreCase("X_element_combinatorial_repeat")) {
			item = createItem("XElementCombinatorialRepeat");
		}else if (childType.equalsIgnoreCase("X_element")) {
			item = createItem("XElement");
		}else if (childType.equalsIgnoreCase("telomeric_repeat")) {
			item = createItem("TelomericRepeat");
		}

		return item;

	}
	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 * @throws Exception
	 */

	private void processChromosomeSequences(Connection connection)
			throws SQLException, ObjectStoreException, Exception {

		ResultSet res = PROCESSOR.getChromosomeSequenceResults(connection);
		System.out.println("Processing ChromosomeSequence...");
		while (res.next()) {

			String featureNo = res.getString("feature_no");
			String chromosomeNo = res.getString("feature_name");
			String residues = res.getString("residues");
			String length = res.getString("seq_length");
			String feature_type = res.getString("feature_type");


			String fixed_chromosome_no = getFixedChrName(chromosomeNo);

			//if (chromosomeNo.equalsIgnoreCase("17")) {
			//chromosomeNo = "chrMito";
			//}else if(!chromosomeNo.equalsIgnoreCase("2-micron")) {
			//chromosomeNo = "chr"+chromosomeNo;
			//}

			if (feature_type.equalsIgnoreCase("chromosome")) {

				Item chr = createItem("Chromosome");
				chr.setAttribute("primaryIdentifier", fixed_chromosome_no);
				chr.setReference("organism", organism);
				chr.setAttribute("length", length);
				chr.setAttribute("featureType", feature_type);

				Item seq = createItem("Sequence");
				seq.setAttribute("residues", residues);
				seq.setAttribute("length", length);

				try {
					store(seq);
				} catch (ObjectStoreException e) {
					throw new ObjectStoreException(e);
				}

				chr.setReference("sequence", seq.getIdentifier());
				chromosomes.put(fixed_chromosome_no, chr.getIdentifier());

				try {
					store(chr);
				} catch (ObjectStoreException e) {
					throw new ObjectStoreException(e);
				}

			} else if (feature_type.equalsIgnoreCase("plasmid")) {

				Item item = createItem("Plasmid");
				item.setAttribute("primaryIdentifier", fixed_chromosome_no); 
				item.setReference("organism", organism);
				plasmids.put(fixed_chromosome_no, item.getIdentifier());
				try {
					store(item);
				} catch (ObjectStoreException e) {
					throw new ObjectStoreException(e);
				}
			}

		}// while

	}

	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storeGenes() throws ObjectStoreException {
		for (Item gene : genes.values()) {
			try {
				store(gene);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}


	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storeInteractionTypes() throws ObjectStoreException {
		for (Item type : interactiontype.values()) {
			try {
				store(type);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}


	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storeInteractionExperiments() throws ObjectStoreException {
		for (Item exp : experimenttype.values()) {
			try {
				store(exp);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}


	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storeInteractionDetails() throws ObjectStoreException {
		for (Item det : interactiondetail.values()) {
			try {
				store(det);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}


	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storePublications() throws ObjectStoreException {
		for (Item pub : publications.values()) {
			try {
				store(pub);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}

	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storePathways() throws ObjectStoreException {
		for (Item path : pathways.values()) {
			try {
				store(path);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}

	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storeInteractions() throws ObjectStoreException {
		for (Item intact : interactions.values()) {
			try {
				store(intact);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}

	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storePhenotypes() throws ObjectStoreException {
		for (Item pheno : phenotypes.values()) {
			try {
				store(pheno);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */

	private void processAllPubs(Connection connection) throws SQLException,
	ObjectStoreException {

		String prevReferenceNo = "";
		String prevPubMedId = "";
		String prevTitle = "";
		String prevCitation = "";
		String prevJournal = "";
		String prevVolume = "";
		String prevPages = "";
		String prevYear = "";
		String prevIssue = "";
		String prevAbst = "";
		String prevDbxRef = "";

		ArrayList<String> hm = new ArrayList<String>();

		Item gene = null;
		boolean firstrow = true;

		ResultSet res = PROCESSOR.getPubAllResults(connection);

		System.out.println("Processing All Publications with Topics...");

		while (res.next()) {

			String referenceNo = res.getString("reference_no");
			String pubMedId = res.getString("pubmed");
			String title = res.getString("title");
			String citation = res.getString("citation");
			String topic = res.getString("literature_topic");
			String journal = res.getString("abbreviation");
			String volume = res.getString("volume");
			String pages = res.getString("page");
			String year = res.getString("year");
			String issue = res.getString("issue");
			String abst = res.getString("abstract");
			String dbxrefid = res.getString("dbxref_id");

			if (firstrow) {
				prevReferenceNo = referenceNo;
				firstrow = false;
			}

			if (!referenceNo.equalsIgnoreCase(prevReferenceNo)) {
				getPub(prevReferenceNo, prevTitle, prevPubMedId, prevCitation,
						hm, prevJournal, prevVolume, prevPages, prevYear,
						prevIssue, prevAbst, prevDbxRef);
				hm.clear();
			}

			if (topic != null) {
				hm.add(new String(topic));
			}

			prevReferenceNo = referenceNo;
			prevTitle = title;
			prevPubMedId = pubMedId;
			prevCitation = citation;
			prevJournal = journal;
			prevVolume = volume;
			prevYear = year;
			prevIssue = issue;
			prevPages = pages;
			prevAbst = abst;
			prevDbxRef = dbxrefid;

		}

		// process the very last reference group
		getPub(prevReferenceNo, prevTitle, prevPubMedId, prevCitation, hm,
				prevJournal, prevVolume, prevPages, prevYear, prevIssue,
				prevAbst, prevDbxRef);
		hm.clear();

	}

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */

	private void processPubsWithFeatures(Connection connection)
			throws SQLException, ObjectStoreException {

		String prevGeneFeatureNo = "";
		String prevReferenceNo = "";
		String prevPubMedId = "";
		String prevTitle = "";
		String prevCitation = "";
		String prevJournal = "";
		String prevVolume = "";
		String prevPages = "";
		String prevYear = "";
		String prevIssue = "";
		String prevDbxRef = "";

		ArrayList<String> hm = new ArrayList<String>();

		Item gene = null;
		boolean firstrow = true;

		ResultSet res = PROCESSOR.getPubWithFeaturesResults(connection);

		System.out
		.println("Processing Publications With Chromosomal Features...");

		while (res.next()) {

			String referenceNo = res.getString("reference_no");
			String geneFeatureNo = res.getString("gene_feature_no");
			String pubMedId = res.getString("pubmed");
			String title = res.getString("title");
			String citation = res.getString("citation");
			String topic = res.getString("literature_topic");
			String journal = res.getString("abbreviation");
			String volume = res.getString("volume");
			String pages = res.getString("page");
			String year = res.getString("year");
			String issue = res.getString("issue");
			String dbxrefid = res.getString("dbxref_id");

			if (!geneFeatureNo.equalsIgnoreCase(prevGeneFeatureNo)) {

				if (!firstrow) {
					getPubAnnot(prevReferenceNo, prevTitle, prevPubMedId,
							prevCitation, hm, gene, prevJournal, prevVolume,
							prevPages, prevYear, prevIssue, prevDbxRef);
					hm.clear();
					prevReferenceNo = referenceNo;
				}
				gene = genes.get(geneFeatureNo);
			}

			if (firstrow) {
				prevReferenceNo = referenceNo;
				firstrow = false;
			}

			if (!referenceNo.equalsIgnoreCase(prevReferenceNo)) {
				getPubAnnot(prevReferenceNo, prevTitle, prevPubMedId,
						prevCitation, hm, gene, prevJournal, prevVolume,
						prevPages, prevYear, prevIssue, prevDbxRef);
				hm.clear();
			}

			if (topic != null) {
				hm.add(new String(topic));
			}

			prevGeneFeatureNo = geneFeatureNo;
			prevReferenceNo = referenceNo;
			prevTitle = title;
			prevPubMedId = pubMedId;
			prevCitation = citation;
			prevJournal = journal;
			prevVolume = volume;
			prevYear = year;
			prevIssue = issue;
			prevPages = pages;
			prevDbxRef = dbxrefid;

		}

		// process the very last reference group
		getPubAnnot(prevReferenceNo, prevTitle, prevPubMedId, prevCitation, hm,
				gene, prevJournal, prevVolume, prevPages, prevYear, prevIssue, prevDbxRef);
		hm.clear();

	}

	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */
	private void processPubsForPhenotypes(Connection connection)
			throws SQLException, ObjectStoreException {

		ResultSet res = PROCESSOR.getPubForPhenotype(connection);

		System.out
		.println("Processing Publications associated with Pheno_annot_no....");

		while (res.next()) {

			String phenoAnnotNo = res.getString("pheno_annot_no");
			String referenceNo = res.getString("reference_no");
			String pubMedId = res.getString("pubmed");
			String status = res.getString("status");
			String title = res.getString("title");
			String volume = res.getString("volume");
			String pages = res.getString("page");
			String year = res.getString("year");
			String issue = res.getString("issue");
			String citation = res.getString("citation");
			String journal = res.getString("abbreviation");
			String dbxrefid = res.getString("dbxref_id");

			getPubPhenotype(phenoAnnotNo, referenceNo, title, pubMedId,
					citation, journal, volume, pages, year, issue, dbxrefid);

		}

	}

	/*
	 * private void processPubs(Connection connection) throws SQLException,
	 * ObjectStoreException { ResultSet res =
	 * PROCESSOR.getPubResults(connection); while (res.next()) {
	 * 
	 * String featureNo = res.getString("reference_no"); String geneFeatureNo =
	 * res.getString("gene_feature_no"); Item gene = genes.get(geneFeatureNo);
	 * if (gene == null) { continue; }
	 * 
	 * String issue = res.getString("issue"); String volume =
	 * res.getString("volume"); String pubMedId = res.getString("pubmed");
	 * String pages = res.getString("page"); String title =
	 * res.getString("title"); String year = res.getString("year"); String
	 * citation = res.getString("citation"); String topic =
	 * res.getString("literature_topic"); String journal =
	 * res.getString("abbreviation");
	 * 
	 * String refId = getPub(featureNo, issue, volume, pubMedId, pages, title,
	 * year, citation); //publication
	 * 
	 * Item item = createItem("PublicationAnnotation");
	 * item.setReference("subject", gene.getIdentifier());
	 * item.setReference("literatureTopic", getLiteratureTopic(topic));
	 * item.addToCollection("publications", refId); try { store(item); } catch
	 * (ObjectStoreException e) { throw new ObjectStoreException(e); }
	 * 
	 * gene.addToCollection("publicationAnnotations", item.getIdentifier());
	 * 
	 * } }
	 */


	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */

	private void processInteractions(Connection connection)
			throws SQLException, ObjectStoreException {

		String dsId = getBioGridDataSet();
		int count = 0;

		ResultSet res = PROCESSOR.getInteractionResults(connection);
		System.out.println("Processing Interactions...");
		while (res.next()) {
			count++;
			String geneFeatureName = res.getString("feature_a");

			Item gene = genesName.get(geneFeatureName); //can save on look-ups here

			String interactionNo = res.getString("interaction_no");
			String referenceNo = res.getString("reference_no");
			String interactionType = res.getString("interaction_type");
			String experimentType = res.getString("experiment_type");
			String annotationType = res.getString("annotation_type");
			String modification = res.getString("modification");

			String interactingGeneFeatureName = res.getString("feature_b");
			Item interactingGene = genesName.get(interactingGeneFeatureName);

			String action = res.getString("action");
			String source = res.getString("source");
			String phenotype = res.getString("phenotype");
			String citation = res.getString("citation");
			String pubmed = res.getString("pubmed");
			String title = res.getString("title");
			String volume = res.getString("volume");
			String page = res.getString("page");
			String year = res.getString("year");
			String issue = res.getString("issue");
			String abbreviation = res.getString("abbreviation");
			String firstAuthor = res.getString("first_author");
			String dbxrefid = res.getString("dbxref_id");

			String interactionRefId = getInteraction1_1(interactionNo,
					referenceNo, interactionType, experimentType,
					annotationType, modification, interactingGene, action, source,
					phenotype, citation, gene, pubmed, title, volume, page,
					year, issue, abbreviation, dsId, firstAuthor, dbxrefid);

		}
	}


	private void processPhenotypes(Connection connection) throws SQLException,
	ObjectStoreException {

		String prevGeneFeatureNo = "";
		String prevGeneFeatureType = "";
		String prevPhenotypeAnnotNo = "";
		String prevExperimentType = "";
		String prevExperimentComment = "";
		String prevMutantType = "";
		String prevQualifier = "";
		String prevObservable = "";

		HashMap<String, String> hm = new HashMap<String, String>();

		Item gene = null;
		boolean firstrow = true;

		ResultSet res = PROCESSOR.getPhenotypeResults(connection);
		System.out.println("Processing Phenotypes...");
		while (res.next()) {

			// use these to switch from feature to next && annotation to next
			String geneFeatureNo = res.getString("feature_no");
			String phenotypeAnnotNo = res.getString("pheno_annotation_no");

			System.out.println("phenotype gene feature no..." + geneFeatureNo);
			
			// set once
			String experimentType = res.getString("experiment_type");
			String experimentComment = res.getString("experiment_comment");
			String mutantType = res.getString("mutant_type");
			String qualifier = res.getString("qualifier");
			if (qualifier == null)
				qualifier = "none";
			String db_observable = res.getString("observable");

			// .. Process - Separate word into parts, change case, put together.
			String firstLetter = db_observable.substring(0, 1); // Get first
			// letter
			String remainder = db_observable.substring(1); // Get remainder of
			// word.
			String observable = firstLetter.toUpperCase() + remainder; // .toLowerCase();

			// add attributes of certain key types
			String key = res.getString("property_type");
			String value = res.getString("property_value");
			String desc = res.getString("property_description");
			String feature_type = res.getString("feature_type");

			if (!geneFeatureNo.equalsIgnoreCase(prevGeneFeatureNo)) {

				if (!firstrow) {
					getPhenotype(prevPhenotypeAnnotNo, prevQualifier,
							prevObservable, prevExperimentType, prevExperimentComment,  prevMutantType,
							hm, gene, prevGeneFeatureType);
					hm.clear();
					prevPhenotypeAnnotNo = phenotypeAnnotNo;
				}
				gene = genes.get(geneFeatureNo);
			}

			if (firstrow) {
				prevPhenotypeAnnotNo = phenotypeAnnotNo;
				firstrow = false;
			}

			if (!phenotypeAnnotNo.equalsIgnoreCase(prevPhenotypeAnnotNo)) {
				getPhenotype(prevPhenotypeAnnotNo, prevQualifier,
						prevObservable, prevExperimentType, prevExperimentComment, prevMutantType, hm,
						gene, prevGeneFeatureType);
				hm.clear();
			}

			if (key != null && value != null) {
				if (key.equalsIgnoreCase("Allele")) {
					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					hm.put("allele", value);
				} else if (key.equalsIgnoreCase("strain_background")) {
					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					hm.put("strainBackground", value);
				} else if (key.equalsIgnoreCase("Chemical_pending")) {

					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					String storedval = (String) hm.get("chemical");
					String newval = "";
					if (storedval != null) {
						newval = storedval + ";" + value;
					} else {
						newval = value;
					}
					hm.put("chemical", newval);

				} else if (key.equalsIgnoreCase("chebi_ontology")) {
					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					String storedval = (String) hm.get("chemical");
					String newval = "";
					if (storedval != null) {
						newval = storedval + ";" + value;
					} else {
						newval = value;
					}
					hm.put("chemical", newval);
				} else if (key.equalsIgnoreCase("Condition")) {

					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					String storedcond = (String) hm.get("condition");
					String newcond = "";
					if (storedcond != null) {
						newcond = storedcond + ";" + value;
					} else {
						newcond = value;
					}

					hm.put("condition", newcond);

				} else if (key.equalsIgnoreCase("Details")) {

					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					String storeddet = (String) hm.get("details");
					String newdet = "";
					if (storeddet != null) {
						newdet = storeddet + ";" + value;
					} else {
						newdet = value;
					}

					hm.put("details", newdet);

				} else if (key.equalsIgnoreCase("Reporter")) {

					if (desc != null) {
						value = value + " (" + desc + ")";
					}
					String storedrep = (String) hm.get("reporter");
					String newrep = "";
					if (storedrep != null) {
						newrep = storedrep + ";" + value;
					} else {
						newrep = value;
					}
					hm.put("reporter", newrep);

				}
			}

			prevGeneFeatureNo = geneFeatureNo;
			prevGeneFeatureType = feature_type;
			prevPhenotypeAnnotNo = phenotypeAnnotNo;
			prevExperimentType = experimentType;
			prevExperimentComment = experimentComment;			
			prevMutantType = mutantType;
			prevQualifier = qualifier;
			prevObservable = observable;

		}

		// process the very last phenotype group
		getPhenotype(prevPhenotypeAnnotNo, prevQualifier, prevObservable,
				prevExperimentType, prevExperimentComment, prevMutantType, hm, gene,
				prevGeneFeatureType);
		hm.clear();

	}

	// private void addCollection(String collectionName) {
	// for (Map.Entry<String, List<String>> entry : featureMap.entrySet()) {
	// String featureNo = entry.getKey();
	// List<String> pubRefIds = entry.getValue();
	// Item gene = genes.get(featureNo);
	// if (gene != null) {
	// gene.setCollection(collectionName, pubRefIds);
	// }
	// }
	// featureMap = new HashMap();
	// }
	//        
	// private void addFeature(String featureNo, String refId) {
	// if (featureMap.get(featureNo) == null) {
	// featureMap.put(featureNo, new ArrayList());
	// }
	// featureMap.get(featureNo).add(refId);
	// }

	private String getLocation(Item subject, String chromosomeRefId,
			String startCoord, String stopCoord, String strand)
					throws ObjectStoreException {

		String start = startCoord;
		String end = stopCoord;

		/*
		 * String start = ""; //(strand.equals("-") ? stopCoord : startCoord);
		 * //was C for crick String end = ""; // (strand.equals("-") ?
		 * startCoord : stopCoord); //was C for crick
		 * 
		 * if(strand.equals("-")){ start = stopCoord; end = startCoord;
		 * System.out.println("start and stop should be reveresed.. " + start +
		 * "  " + end); }else{ end = stopCoord; start = startCoord; }
		 */

		/*
		 * if (StringUtils.isEmpty(startCoord)) { start = "0"; } if
		 * (StringUtils.isEmpty(stopCoord)) { end = "0"; }
		 */

		if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end)) {
			subject.setAttribute("length", getLength(start, end));
		}

		Item location = createItem("Location");

		if (!StringUtils.isEmpty(start))
			location.setAttribute("start", start);
		if (!StringUtils.isEmpty(end))
			location.setAttribute("end", end);
		if (!StringUtils.isEmpty(strand))
			location.setAttribute("strand", strand);

		location.setReference("feature", subject);
		location.setReference("locatedOn", chromosomeRefId);

		try {
			store(location);
		} catch (ObjectStoreException e) {
			throw new ObjectStoreException(e);
		}
		return location.getIdentifier();
	}

	private String getLength(String start, String end)
			throws NumberFormatException {
		Integer a = new Integer(start);
		Integer b = new Integer(end);

		// if the coordinates are on the crick strand, they need to be reversed
		// or they
		// result in a negative number
		if (a.compareTo(b) > 0) {
			a = new Integer(end);
			b = new Integer(start);
		}

		Integer length = new Integer(b.intValue() - a.intValue());
		return length.toString();
	}

	private String getChromosome(String identifier) throws ObjectStoreException {
		if (StringUtils.isEmpty(identifier)) {
			return null;
		}
		String refId = chromosomes.get(identifier);
		if (refId == null) {
			Item item = createItem("Chromosome");
			item.setAttribute("primaryIdentifier", identifier);
			item.setReference("organism", organism);
			refId = item.getIdentifier();
			chromosomes.put(identifier, refId);
			try {
				store(item);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
		return refId;
	}

	private String getBioGridDataSet() throws ObjectStoreException {

		Item item = createItem("DataSet");
		item.setAttribute("name", "BioGRID interaction data set");


		Item ds = createItem("DataSource");
		ds.setAttribute("name", "BioGRID");
		ds.addToCollection("dataSets", item.getIdentifier());
		try {
			store(item);
			store(ds);
		} catch (ObjectStoreException e) {
			throw new ObjectStoreException(e);
		}

		return item.getIdentifier();


	}
	private String getPlasmid(String identifier) throws ObjectStoreException { // String
		// id,
		if (StringUtils.isEmpty(identifier)) {
			return null;
		}
		String refId = plasmids.get(identifier);
		if (refId == null) {
			Item item = createItem("Plasmid");
			item.setAttribute("primaryIdentifier", identifier);
			item.setReference("organism", organism);
			refId = item.getIdentifier();
			plasmids.put(identifier, refId);
			try {
				store(item);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
		return refId;
	}

	private String getSequence(String id, String residues, String length)
			throws ObjectStoreException {
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		String refId = sequences.get(id);
		if (refId == null) {
			Item item = createItem("Sequence");
			item.setAttribute("residues", residues);
			item.setAttribute("length", length);
			// item.setReference("organism", organism);
			refId = item.getIdentifier();
			sequences.put(id, refId);
			try {
				store(item);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
		return refId;
	}

	private String getSynonym(String subjectId, String type, String value)
			throws ObjectStoreException {
		String key = subjectId + type + value;
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String refId = synonyms.get(key);
		if (refId == null) {
			Item syn = createItem("Synonym");
			syn.setReference("subject", subjectId);
			// syn.setAttribute("type", type);
			syn.setAttribute("value", value);
			refId = syn.getIdentifier();
			// synonyms.get(key);
			try {
				store(syn);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
		return refId;
	}


	private String getPathway(String geneIdentifier, String id, String name)
			throws ObjectStoreException {

		Item crf = pathways.get(id);
		if (crf == null) {

			crf = createItem("Pathway");
			crf.setAttribute("identifier", id);
			crf.setAttribute("name", name);
			pathways.put(id, crf);

			crf.addToCollection("genes", geneIdentifier);

		}else{
			crf.addToCollection("genes", geneIdentifier);
		}

		String refId = crf.getIdentifier();
		return refId;

	}

	private String getCrossReference(String subjectId, String id, String source)
			throws ObjectStoreException {

		String refId = "";
		Item crf = createItem("CrossReference");
		crf.setReference("subject", subjectId);
		crf.setAttribute("identifier", id);
		crf.setAttribute("dbxrefsource", source);

		String dsId = datasources.get(source);
		if (dsId == null) {
			Item ds = createItem("DataSource");
			ds.setAttribute("name", source);
			try {
				store(ds);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}

			crf.setReference("source", ds.getIdentifier());
			datasources.put(source, ds.getIdentifier());
		} else {
			crf.setReference("source", dsId);
		}

		try {
			store(crf);
		} catch (ObjectStoreException e) {
			throw new ObjectStoreException(e);
		}

		refId = crf.getIdentifier();
		return refId;

	}

	private void getPhenotype(String prevPhenotypeAnnotNo,
			String prevQualifier, String prevObservable,
			String prevExperimentType, String prevExperimentComment, String prevMutantType, HashMap hm,
			Item gene, String feature_type) throws ObjectStoreException {

		Item pheno = createItem("Phenotype");
		pheno.setAttribute("qualifier", prevQualifier);

		if (prevObservable != null && StringUtils.isNotEmpty(prevObservable)) {
			pheno.setAttribute("observable", prevObservable);
		}
		if (prevExperimentType != null
				&& StringUtils.isNotEmpty(prevExperimentType)) {
			pheno.setAttribute("experimentType", prevExperimentType);
		}
		if (prevExperimentComment != null
				&& StringUtils.isNotEmpty(prevExperimentComment)) {
			pheno.setAttribute("experimentComment", prevExperimentComment);
		}
		if (prevMutantType != null && StringUtils.isNotEmpty(prevMutantType)) {
			pheno.setAttribute("mutantType", prevMutantType);
		}

		for (Iterator iter = hm.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			pheno.setAttribute(key, value);
		}

		if (feature_type.equalsIgnoreCase("not physically mapped")) {
			pheno.addToCollection("notphysicallymapped", gene.getIdentifier());
		} else {
			pheno.addToCollection("genes", gene.getIdentifier());
		}

		phenotypes.put(prevPhenotypeAnnotNo, pheno);

		hm.clear();

	}

	/**
	 * 
	 * @param interactionNo
	 * @param referenceNo
	 * @param interactionType
	 * @param experimentType
	 * @param annotationType
	 * @param modification
	 * @param interactingGene
	 * @param action
	 * @param source
	 * @param phenotype
	 * @param citation
	 * @param gene
	 * @param pubMedId
	 * @param title
	 * @param volume
	 * @param page
	 * @param year
	 * @param issue
	 * @param journal
	 * @param dsetIdentifier
	 * @param firstAuthor
	 * @param dbxrefid
	 * @return
	 * @throws ObjectStoreException
	 */
	private String getInteraction1_1(String interactionNo, String referenceNo,
			String interactionType, String experimentType,
			String annotationType, String modification, Item interactingGene, String action,
			String source, String phenotype, String citation, Item gene,
			String pubMedId, String title, String volume, String page,
			String year, String issue, String journal, String dsetIdentifier, String firstAuthor, String dbxrefid)
					throws ObjectStoreException {

		Item item = getInteractionItem(gene.getIdentifier(), interactingGene.getIdentifier());	
		Item detail = createItem("InteractionDetail");		   

		detail.setAttribute("type", interactionType);		
		detail.setAttribute("annotationType", annotationType);
		detail.setAttribute("experimentType", experimentType);
		if (modification != null) detail.setAttribute("modification", modification);
		if (phenotype != null) detail.setAttribute("phenotype", phenotype);
		detail.setAttribute("role1", action);
		detail.addToCollection("allInteractors", interactingGene.getIdentifier());
		//detail.addToCollection("interactingGenes", interactingGene.getIdentifier());
		detail.addToCollection("dataSets", dsetIdentifier);		

		Item storedInteractionType = interactiontype.get(interactionType);
		if (storedInteractionType != null) {
			detail.setReference("relationshipType", storedInteractionType.getIdentifier());
		} else {
			storedInteractionType = createItem("InteractionTerm");
			if (StringUtils.isNotEmpty(interactionType)) {
				storedInteractionType.setAttribute("name", interactionType);
			}
			detail.setReference("relationshipType", storedInteractionType.getIdentifier());
			interactiontype.put(interactionType, storedInteractionType);
		}

		String unqName = firstAuthor+"-"+pubMedId;

		//add publication as experiment type
		Item storedExperimentType = experimenttype.get(unqName);		
		if(storedExperimentType == null) {			
			storedExperimentType = createItem("InteractionExperiment");
			storedExperimentType.setAttribute("name", unqName);	
			experimenttype.put(unqName, storedExperimentType);
		}

		//add publication as reference on experiment
		Item storedRef = publications.get(referenceNo);

		if (storedRef != null) {
			storedExperimentType.setReference("publication", storedRef.getIdentifier());
		} else {

			Item pub = createItem("Publication");

			if (StringUtils.isNotEmpty(pubMedId)) {
				pub.setAttribute("pubMedId", pubMedId);
			}
			if (StringUtils.isNotEmpty(dbxrefid)) {
				pub.setAttribute("sgdDbXrefId", dbxrefid);
			}
			if (StringUtils.isNotEmpty(title)) {
				pub.setAttribute("title", title);
			}
			if (StringUtils.isNotEmpty(citation)) {
				pub.setAttribute("citation", citation);
			}
			if (StringUtils.isNotEmpty(journal)) {
				pub.setAttribute("journal", journal);
			}
			if (StringUtils.isNotEmpty(volume)) {
				pub.setAttribute("volume", volume);
			}
			if (StringUtils.isNotEmpty(page)) {
				pub.setAttribute("pages", page);
			}
			if (StringUtils.isNotEmpty(year)) {
				pub.setAttribute("year", year);
			}
			if (StringUtils.isNotEmpty(issue)) {
				pub.setAttribute("issue", issue);
			}
			publications.put(referenceNo, pub);
			storedExperimentType.setReference("publication", pub.getIdentifier());
		}

		detail.setReference("experiment", storedExperimentType.getIdentifier());	
		detail.setReference("interaction", item);
		//interactiondetail.put(detail.getIdentifier(), detail);	

		try {
			store(detail);
		} catch (ObjectStoreException e) {
			throw new ObjectStoreException(e);
		}

		interactions.put(item.getIdentifier(), item);	
		String refId = item.getIdentifier();
		return refId;


	}

	private String getInteraction1_1_0_98(String interactionNo, String referenceNo,
			String interactionType, String experimentType,
			String annotationType, String modification, Item interactingGene, String action,
			String source, String phenotype, String citation, Item gene,
			String pubMedId, String title, String volume, String page,
			String year, String issue, String journal, String dsetIdentifier, String firstAuthor, String dbxrefid)
					throws ObjectStoreException {

		Item item = getInteractionItem(gene.getIdentifier(), interactingGene.getIdentifier());	


		//this would be for old		
		item.setAttribute("interactionType", interactionType);		
		item.setAttribute("annotationType", annotationType);
		item.setAttribute("experimentType", experimentType);
		if (modification != null) item.setAttribute("modification", modification);
		if (phenotype != null) item.setAttribute("phenotype", phenotype);
		item.setAttribute("role", action);
		item.setReference("gene", gene.getIdentifier());
		item.addToCollection("interactingGenes", interactingGene.getIdentifier());
		item.addToCollection("dataSets", dsetIdentifier);		

		Item storedInteractionType = interactiontype.get(interactionType);
		if (storedInteractionType != null) {
			item.setReference("type", storedInteractionType.getIdentifier());
		} else {
			storedInteractionType = createItem("InteractionTerm");
			if (StringUtils.isNotEmpty(interactionType)) {
				storedInteractionType.setAttribute("name", interactionType);
			}
			item.setReference("type", storedInteractionType.getIdentifier());
			interactiontype.put(interactionType, storedInteractionType);
		}

		//item.setReference("experiment", storedExperimentType.getIdentifier()); - had to move this old down	

		//this would be for new
		Item detail = createItem("InteractionDetail");		   

		detail.setAttribute("type", interactionType);		
		detail.setAttribute("annotationType", annotationType);
		detail.setAttribute("experimentType", experimentType);
		if (modification != null) detail.setAttribute("modification", modification);
		if (phenotype != null) detail.setAttribute("phenotype", phenotype);
		detail.setAttribute("role1", action);
		detail.addToCollection("allInteractors", interactingGene.getIdentifier());
		//detail.addToCollection("interactingGenes", interactingGene.getIdentifier());
		detail.addToCollection("dataSets", dsetIdentifier);		

		//Item storedInteractionType = interactiontype.get(interactionType);
		if (storedInteractionType != null) {
			detail.setReference("relationshipType", storedInteractionType.getIdentifier());
		} else {
			storedInteractionType = createItem("InteractionTerm");
			if (StringUtils.isNotEmpty(interactionType)) {
				storedInteractionType.setAttribute("name", interactionType);
			}
			detail.setReference("relationshipType", storedInteractionType.getIdentifier());
			interactiontype.put(interactionType, storedInteractionType);
		}

		String unqName = firstAuthor+"-"+pubMedId;

		//add publication as experiment type
		Item storedExperimentType = experimenttype.get(unqName);		
		if(storedExperimentType == null) {			
			storedExperimentType = createItem("InteractionExperiment");
			storedExperimentType.setAttribute("name", unqName);	
			experimenttype.put(unqName, storedExperimentType);
		}

		//add publication as reference on experiment
		Item storedRef = publications.get(referenceNo);

		if (storedRef != null) {
			storedExperimentType.setReference("publication", storedRef.getIdentifier());
		} else {

			Item pub = createItem("Publication");

			if (StringUtils.isNotEmpty(pubMedId)) {
				pub.setAttribute("pubMedId", pubMedId);
			}
			if (StringUtils.isNotEmpty(dbxrefid)) {
				pub.setAttribute("sgdDbXrefId", dbxrefid);
			}
			if (StringUtils.isNotEmpty(title)) {
				pub.setAttribute("title", title);
			}
			if (StringUtils.isNotEmpty(citation)) {
				pub.setAttribute("citation", citation);
			}
			if (StringUtils.isNotEmpty(journal)) {
				pub.setAttribute("journal", journal);
			}
			if (StringUtils.isNotEmpty(volume)) {
				pub.setAttribute("volume", volume);
			}
			if (StringUtils.isNotEmpty(page)) {
				pub.setAttribute("pages", page);
			}
			if (StringUtils.isNotEmpty(year)) {
				pub.setAttribute("year", year);
			}
			if (StringUtils.isNotEmpty(issue)) {
				pub.setAttribute("issue", issue);
			}
			publications.put(referenceNo, pub);
			storedExperimentType.setReference("publication", pub.getIdentifier());
		}

		detail.setReference("experiment", storedExperimentType.getIdentifier());	
		detail.setReference("interaction", item);

		item.setReference("experiment", storedExperimentType.getIdentifier()); //this is for old
		//interactiondetail.put(detail.getIdentifier(), detail);	

		try {
			store(detail);
		} catch (ObjectStoreException e) {
			throw new ObjectStoreException(e);
		}

		interactions.put(item.getIdentifier(), item);	
		String refId = item.getIdentifier();
		return refId;


	}


	private Item getInteractionItem(String refId, String gene2RefId) throws ObjectStoreException {
		MultiKey key = new MultiKey(refId, gene2RefId);
		Item interaction = interactionsnew.get(key);
		if (interaction == null) {
			interaction = createItem("Interaction");
			interaction.setReference("gene1", refId);
			interaction.setReference("gene2", gene2RefId);
			//interactionsnew.put(key, interaction);
			//store(interaction);
		}
		return interaction;
	}


	/**
	 * 
	 * @param interactionNo
	 * @param referenceNo
	 * @param interactionType
	 * @param experimentType
	 * @param annotationType
	 * @param modification
	 * @param interactingGene
	 * @param action
	 * @param source
	 * @param phenotype
	 * @param citation
	 * @param gene
	 * @param pubMedId
	 * @param title
	 * @param volume
	 * @param page
	 * @param year
	 * @param issue
	 * @param journal
	 * @param dsetIdentifier
	 * @param firstAuthor
	 * @param dbxrefid
	 * @return
	 * @throws ObjectStoreException
	 */
	private String getInteraction0_98(String interactionNo, String referenceNo,
			String interactionType, String experimentType,
			String annotationType, String modification, Item interactingGene, String action,
			String source, String phenotype, String citation, Item gene,
			String pubMedId, String title, String volume, String page,
			String year, String issue, String journal, String dsetIdentifier, String firstAuthor, String dbxrefid)
					throws ObjectStoreException {

		Item item = createItem("Interaction");

		item.setAttribute("interactionType", interactionType);		
		item.setAttribute("annotationType", annotationType);
		item.setAttribute("experimentType", experimentType);
		if (modification != null) item.setAttribute("modification", modification);
		if (phenotype != null) item.setAttribute("phenotype", phenotype);
		item.setAttribute("role", action);
		item.setReference("gene", gene.getIdentifier());
		item.addToCollection("interactingGenes", interactingGene.getIdentifier());
		item.addToCollection("dataSets", dsetIdentifier);		

		Item storedInteractionType = interactiontype.get(interactionType);
		if (storedInteractionType != null) {
			item.setReference("type", storedInteractionType.getIdentifier());
		} else {
			storedInteractionType = createItem("InteractionTerm");
			if (StringUtils.isNotEmpty(interactionType)) {
				storedInteractionType.setAttribute("name", interactionType);
			}
			item.setReference("type", storedInteractionType.getIdentifier());
			interactiontype.put(interactionType, storedInteractionType);
		}

		String unqName = firstAuthor+"-"+pubMedId;

		//add publication as experiment type
		Item storedExperimentType = experimenttype.get(unqName);		
		if(storedExperimentType == null) {			
			storedExperimentType = createItem("InteractionExperiment");
			storedExperimentType.setAttribute("name", unqName);	
			experimenttype.put(unqName, storedExperimentType);
		}		

		//add publication as reference on experiment
		Item storedRef = publications.get(referenceNo);

		if (storedRef != null) {
			storedExperimentType.setReference("publication", storedRef.getIdentifier());
		} else {

			Item pub = createItem("Publication");

			if (StringUtils.isNotEmpty(pubMedId)) {
				pub.setAttribute("pubMedId", pubMedId);
			}
			if (StringUtils.isNotEmpty(dbxrefid)) {
				pub.setAttribute("sgdDbXrefId", dbxrefid);
			}
			if (StringUtils.isNotEmpty(title)) {
				pub.setAttribute("title", title);
			}
			if (StringUtils.isNotEmpty(citation)) {
				pub.setAttribute("citation", citation);
			}
			if (StringUtils.isNotEmpty(journal)) {
				pub.setAttribute("journal", journal);
			}
			if (StringUtils.isNotEmpty(volume)) {
				pub.setAttribute("volume", volume);
			}
			if (StringUtils.isNotEmpty(page)) {
				pub.setAttribute("pages", page);
			}
			if (StringUtils.isNotEmpty(year)) {
				pub.setAttribute("year", year);
			}
			if (StringUtils.isNotEmpty(issue)) {
				pub.setAttribute("issue", issue);
			}
			publications.put(referenceNo, pub);
			storedExperimentType.setReference("publication", pub.getIdentifier());
		}

		item.setReference("experiment", storedExperimentType.getIdentifier());	
		interactions.put(item.getIdentifier(), item);

		String refId = item.getIdentifier();
		return refId;


	}


	private String getLiteratureTopic(String topic) throws ObjectStoreException {
		String refId = literatureTopics.get(topic);
		if (refId == null) {
			Item item = createItem("LiteratureTopic");
			item.setAttribute("name", topic);
			try {
				store(item);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
			refId = item.getIdentifier();
			literatureTopics.put(topic, refId);
		}
		return refId;
	}

	private void getPub(String referenceNo, String title, String pubMedId,
			String citation, ArrayList hm, String journal, String volume,
			String pages, String year, String issue, String abst, String dbxrefid)
					throws ObjectStoreException {

		Item storedRef = publications.get(referenceNo);

		if (storedRef == null) {

			Item item = createItem("Publication");

			if (StringUtils.isNotEmpty(pubMedId)) {
				item.setAttribute("pubMedId", pubMedId);
			}
			if (StringUtils.isNotEmpty(dbxrefid)) {
				item.setAttribute("sgdDbXrefId", dbxrefid);
			}
			if (StringUtils.isNotEmpty(title)) {
				item.setAttribute("title", title);
			}
			if (StringUtils.isNotEmpty(citation)) {
				item.setAttribute("citation", citation);
			}
			if (StringUtils.isNotEmpty(journal)) {
				item.setAttribute("journal", journal);
			}
			if (StringUtils.isNotEmpty(volume)) {
				item.setAttribute("volume", volume);
			}
			if (StringUtils.isNotEmpty(pages)) {
				item.setAttribute("pages", pages);
			}
			if (StringUtils.isNotEmpty(year)) {
				item.setAttribute("year", year);
			}
			if (StringUtils.isNotEmpty(issue)) {
				item.setAttribute("issue", issue);
			}
			if (StringUtils.isNotEmpty(abst)) {
				item.setAttribute("summary", abst);
			}

			Iterator iter = hm.iterator();
			while (iter.hasNext()) {
				String value = (String) iter.next();
				item.addToCollection("literatureTopics",
						getLiteratureTopic(value));
			}
			publications.put(referenceNo, item);
		}

	}
	
	
	private Item getExistingPub(String referenceNo)
					throws ObjectStoreException {

		Item storedRef = publications.get(referenceNo);

		return storedRef;

	}
	

	private void getPubPhenotype(String phenoAnnotNo, String prevReferenceNo,
			String title, String pubMedId, String citation, String journal,
			String volume, String pages, String year, String issue, String dbxrefid)
					throws ObjectStoreException {

		Item storedPheno = phenotypes.get(phenoAnnotNo);

		if (storedPheno != null) {

			Item storedRef = publications.get(prevReferenceNo);

			if (storedRef != null) {
				storedPheno.addToCollection("publications", storedRef
						.getIdentifier());
				storedRef.addToCollection("phenotypes", storedPheno.getIdentifier());
			} else {
				Item item = createItem("Publication");

				if (StringUtils.isNotEmpty(pubMedId)) {
					item.setAttribute("pubMedId", pubMedId);
				}
				if (StringUtils.isNotEmpty(dbxrefid)) {
					item.setAttribute("sgdDbXrefId", dbxrefid);
				}
				if (StringUtils.isNotEmpty(title)) {
					item.setAttribute("title", title);
				}
				if (StringUtils.isNotEmpty(citation)) {
					item.setAttribute("citation", citation);
				}
				if (StringUtils.isNotEmpty(journal)) {
					item.setAttribute("journal", journal);
				}
				if (StringUtils.isNotEmpty(volume)) {
					item.setAttribute("volume", volume);
				}
				if (StringUtils.isNotEmpty(pages)) {
					item.setAttribute("pages", pages);
				}
				if (StringUtils.isNotEmpty(year)) {
					item.setAttribute("year", year);
				}
				if (StringUtils.isNotEmpty(issue)) {
					item.setAttribute("issue", issue);
				}

				publications.put(prevReferenceNo, item);

				storedPheno.addToCollection("publications", item.getIdentifier());
				item.addToCollection("phenotypes", storedPheno.getIdentifier());

			}

		}

	}

	/**
	 * 
	 * @param featureNo
	 * @param issue
	 * @param volume
	 * @param pubMedId
	 * @param pages
	 * @param title
	 * @param year
	 * @param citation
	 * @return
	 * @throws ObjectStoreException
	 */
	private void getPubAnnot(String referenceNo, String title, String pubMedId,
			String citation, ArrayList hm, Item gene, String journal,
			String volume, String pages, String year, String issue, String dbxrefid)
					throws ObjectStoreException {

		Item pubAnnot = createItem("PublicationAnnotation");
		Item storedRef = publications.get(referenceNo);

		if (storedRef == null) {
			Item item = createItem("Publication");

			if (StringUtils.isNotEmpty(pubMedId)) {
				item.setAttribute("pubMedId", pubMedId);
			}
			if (StringUtils.isNotEmpty(dbxrefid)) {
				item.setAttribute("sgdDbXrefId", dbxrefid);
			}
			if (StringUtils.isNotEmpty(title)) {
				item.setAttribute("title", title);
			}
			if (StringUtils.isNotEmpty(citation)) {
				item.setAttribute("citation", citation);
			}
			if (StringUtils.isNotEmpty(journal)) {
				item.setAttribute("journal", journal);
			}
			if (StringUtils.isNotEmpty(volume)) {
				item.setAttribute("volume", volume);
			}
			if (StringUtils.isNotEmpty(pages)) {
				item.setAttribute("pages", pages);
			}
			if (StringUtils.isNotEmpty(year)) {
				item.setAttribute("year", year);
			}
			if (StringUtils.isNotEmpty(issue)) {
				item.setAttribute("issue", issue);
			}

			Iterator iter = hm.iterator();
			while (iter.hasNext()) {
				String value = (String) iter.next();
				pubAnnot.addToCollection("literatureTopics",
						getLiteratureTopic(value));
			}

			//item.addToCollection("genes", gene.getIdentifier());
			item.addToCollection("bioEntities", gene.getIdentifier());

			publications.put(referenceNo, item);

			pubAnnot.setReference("publication", item.getIdentifier());
			pubAnnot.addToCollection("genes", gene.getIdentifier());

			// in order for the publication enrichment to work, genes needs to
			// have publications directly as a collection, I think.
			gene.addToCollection("publications", item.getIdentifier());

		} else {
			// this means this publication appears for a different gene, lets
			// add the literature topics
			Iterator iter = hm.iterator();
			while (iter.hasNext()) {
				String value = (String) iter.next();
				pubAnnot.addToCollection("literatureTopics",
						getLiteratureTopic(value));
			}

			pubAnnot.setReference("publication", storedRef.getIdentifier());
			pubAnnot.addToCollection("genes", gene.getIdentifier());

			//storedRef.addToCollection("genes", gene.getIdentifier());
			storedRef.addToCollection("bioEntities", gene.getIdentifier());

			gene.addToCollection("publications", storedRef.getIdentifier());
		}

		try {
			store(pubAnnot);
		} catch (ObjectStoreException e) {
			throw new ObjectStoreException(e);
		}

	}

	/**
	 * 
	 * @param featureNo
	 * @param issue
	 * @param volume
	 * @param pubMedId
	 * @param pages
	 * @param title
	 * @param year
	 * @param citation
	 * @return
	 * @throws ObjectStoreException
	 */
	/*
	 * private String getPub(String featureNo, String issue, String volume,
	 * String pubMedId, String pages, String title, String year, String
	 * citation) throws ObjectStoreException { String refId =
	 * publications.get(featureNo); if (refId == null) { Item item =
	 * createItem("Publication"); if (StringUtils.isNotEmpty(issue)) {
	 * item.setAttribute("issue", issue); } if
	 * (StringUtils.isNotEmpty(pubMedId)) { item.setAttribute("pubMedId",
	 * pubMedId); } if (StringUtils.isNotEmpty(title)) {
	 * item.setAttribute("title", title); } if (StringUtils.isNotEmpty(volume))
	 * { item.setAttribute("volume", volume); } item.setAttribute("year", year);
	 * if (StringUtils.isNotEmpty(pages)) { item.setAttribute("pages", pages); }
	 * if (StringUtils.isNotEmpty(citation)) { item.setAttribute("citation",
	 * citation); } refId = item.getIdentifier(); publications.put(featureNo,
	 * refId);
	 * 
	 * try { store(item); } catch (ObjectStoreException e) { throw new
	 * ObjectStoreException(e); } } return refId; }
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataSetTitle(@SuppressWarnings("unused") int taxonId) {
		return DATASET_TITLE;
	}
}
