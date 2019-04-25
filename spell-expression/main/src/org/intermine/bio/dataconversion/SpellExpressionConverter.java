package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author
 */
public class SpellExpressionConverter extends BioDBConverter {
	// 
	private static final String DATASET_TITLE = "Spell Expression Data";
	private static final String DATA_SOURCE_NAME = "SPELL";
	private static final SpellExpressionProcessor PROCESSOR = new SpellExpressionProcessor();
	private Map<String, Item> genes = new HashMap();
	private Map<String, Item> datasets = new HashMap();
	//private Map<String, String> uniqconds = new HashMap()
	private Map<String, Item> conditions = new HashMap();
	private Map<String, Item> tags = new HashMap();
	private ArrayList<String> filenames = new ArrayList();
	private static final String TAXON_ID = "4932";
	private Item organism;

	/**
	 * Construct a new SpellExpressionConverter.
	 * 
	 * @param database
	 *            the database to read from
	 * @param model
	 *            the Model used by the object store we will write to with the
	 *            ItemWriter
	 * @param writer
	 *            an ItemWriter used to handle Items created
	 */
	public SpellExpressionConverter(Database database, Model model,
			ItemWriter writer) throws ObjectStoreException {
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
		Connection connection = getDatabase().getConnection();
		processDataSetFiles(connection); //get all dataset file names
		processDataSets(connection); //process each file
		storeDataSets();
		//storeDataSetTags();
		storeGenes();
	}
	
	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */
	private void processDataSetFiles(Connection connection) throws SQLException, ObjectStoreException {

		ResultSet res = PROCESSOR.getDataSetFileNames(connection);
		System.out.println("Processing DataSet Files...");

		while (res.next()) {
			String fileName = res.getString("filename");
			filenames.add(fileName);			
		}
	}
	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 * @throws ObjectStoreException
	 */
	private void processDataSets(Connection connection) throws SQLException,
	ObjectStoreException {
		
		int count = 0;
		
		
    Iterator it = filenames.iterator();
    while(it.hasNext() && count < 10) { //&& count < 10
    	count++; 
    	String filename = (String) it.next();
    	System.out.println("Processing DataSet No. ..."+ count + "   " +filename);
    	
		ResultSet res = PROCESSOR.getDataSets(connection, filename);
		conditions = new HashMap();  //reset conditions for each dataset
		while (res.next()) {
			String pubmedID = res.getString("pubmedID");
			String fileName = res.getString("filename");
			String geoID = res.getString("geoID");
			String platformID = res.getString("platformID");
			String channelCount = res.getString("channelCount");
			String datasetName = res.getString("d.name");
			String description = res.getString("description");
			String numCond = res.getString("num_conds");
			String numGenes = res.getString("num_genes");
			String author = res.getString("author");
			String allAuthors = res.getString("all_authors");
			String title = res.getString("title");
			String journal = res.getString("journal");
			String pubYear = res.getString("pub_year");
			String condDesc = res.getString("cond_descs");
			String tags = res.getString("tags");
			String geneName = res.getString("g.name");
			String dataTable = res.getString("data_table");

			// create gene first time you see it
			Item gene = getGene(geneName);

			// create dataset first time you see it			
			Item dataset = getDataSet(pubmedID, fileName, geoID, platformID,
					channelCount, datasetName, description, numCond, numGenes,
					author, allAuthors, title, journal, pubYear, tags);		
			System.out.println("gene: "+geneName + "    condDesc: "+ condDesc + " data_table: " + dataTable);
			
			// add score to gene - using the condition and dataset info
			getConditionScore(dataset, condDesc, dataTable, geneName);

		}	
    }//while files
    
	}
	/**
	 * 
	 * @param geneName
	 * @return
	 * @throws ObjectStoreException
	 */

	private Item getGene(String geneName) throws ObjectStoreException {

		Item item = genes.get(geneName);

		if (item == null) {
			item = createItem("Gene");
			item.setAttribute("secondaryIdentifier", geneName);
			item.setReference("organism", organism.getIdentifier());
			genes.put(geneName, item);
		}

		return item;
	}

	/**
	 * 
	 * @param pubmedID
	 * @param fileName
	 * @param geoID
	 * @param platformID
	 * @param channelCount
	 * @param datasetName
	 * @param description
	 * @param numCond
	 * @param numGenes
	 * @param author
	 * @param allAuthors
	 * @param title
	 * @param journal
	 * @param publicationYear
	 * @param tags
	 * @return
	 * @throws ObjectStoreException
	 */

	private Item getDataSet(String pubmedID, String fileName, String geoID,
			String platformID, String channelCount, String datasetName,
			String description, String numCond, String numGenes, String author,
			String allAuthors, String title, String journal,
			String publicationYear, String kwtags) throws ObjectStoreException {
		
		Item item = datasets.get(fileName);

		if (item == null) {

			item = createItem("ExpressionDataSet");
			
			item.setAttribute("pubmedID", pubmedID);
			item.setAttribute("fileName", fileName);
			item.setAttribute("geoID", geoID);
			item.setAttribute("platformID", platformID);
			item.setAttribute("channelCount", channelCount);
			item.setAttribute("name", datasetName);
			item.setAttribute("description", description);
			item.setAttribute("numConds", numCond);
			item.setAttribute("numGenes", numGenes);
			item.setAttribute("author", author);
			item.setAttribute("allAuthors", allAuthors);
			item.setAttribute("title", title);
			item.setAttribute("journal", journal);
			item.setAttribute("publicationYear", publicationYear);					
			//item.setAttribute("tags", tags); store keywords as collection on spelldataset to make it querieable
						
			if(kwtags.contains("|")){
		 								
				String[] keywords = kwtags.split("\\|");
				
				for (int i = 0; i < keywords.length; i++) {					
			        String kw = keywords[i];
			       
			        Item dtag = tags.get(kw);
			        
					if(dtag == null){
                    
						dtag = createItem("ExpressionDataSetTag");
						dtag.setAttribute("tagname", kw);
                           try {
                                 store(dtag);
                            } catch (ObjectStoreException e) {
                                 throw new ObjectStoreException(e);
                            }
						tags.put(kw, dtag);
					}
					item.addToCollection("expressiondatasettags", dtag.getIdentifier());
					
				}
				
			}else{
				Item dtag = tags.get(kwtags);
				
				if(dtag == null){
					dtag = createItem("ExpressionDataSetTag");
					dtag.setAttribute("tagname", kwtags);
                                  try {
                                         store(dtag);
                                 } catch (ObjectStoreException e) {
                                         throw new ObjectStoreException(e);
                                 }
					tags.put(kwtags, dtag);
				}
			         item.addToCollection("expressiondatasettags", dtag.getIdentifier());
				
			}
			System.out.println("pubmed: "+pubmedID + "    geoID: "+ geoID + " datasetName: " + datasetName);								
			datasets.put(fileName, item);
		}

		return item;
	}
	/**
	 * 
	 * @param dataSet
	 * @param condDesc
	 * @param dataTable
	 * @param geneName
	 * @throws ObjectStoreException
	 */
	private void getConditionScore(Item dataSet, String condDesc,
			String dataTable, String geneName) throws ObjectStoreException {

		Item gene = genes.get(geneName);
		String newconds = condDesc.replaceAll("~", "|");
	 
		String[] expconditions = newconds.split("\\|");
		String[] scores = dataTable.split(",");
       
		
		for (int i = 0; i < scores.length; i++) {

			String cond = expconditions[i];
			String condscore = scores[i];
			System.out.println("cond:" + cond + "   score:"+ condscore);
			
			Item dcond = conditions.get(cond);
			if (dcond == null) {
				
				 dcond = createItem("ExpressionCondition");
				 dcond.setAttribute("conditionname", cond);
				 dcond.setAttribute("ordernumber", String.valueOf(i));
				 dcond.setReference("expressiondataset",dataSet.getIdentifier());
				try {
					store(dcond);
				} catch (ObjectStoreException e) {
					throw new ObjectStoreException(e);
				}
				conditions.put(cond, dcond);
			}
			
			dataSet.addToCollection("expressionconditions", dcond.getIdentifier());
			
			// tie up the score with gene
			Item score = createItem("ExpressionScore");
			if(!condscore.equals("NA")) {
				score.setAttribute("score", condscore);
			}
			score.setReference("expressioncondition", dcond.getIdentifier());
			
			try {
				store(score);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
			
			gene.addToCollection("expressionScores", score.getIdentifier());

		}

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

	private void storeDataSets() throws ObjectStoreException {
		for (Item dataset : datasets.values()) {
			try {
				store(dataset);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
		}
	}
	
	/**
	 * 
	 * @throws ObjectStoreException
	 */

	private void storeDataSetTags() throws ObjectStoreException {
		for (Item datasettag : tags.values()) {
			try {
				store(datasettag);
			} catch (ObjectStoreException e) {
				throw new ObjectStoreException(e);
			}
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
