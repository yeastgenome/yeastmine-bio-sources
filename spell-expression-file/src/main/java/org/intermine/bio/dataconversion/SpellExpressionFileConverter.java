package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2019 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;
import org.apache.tools.ant.BuildException;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Map;

/**
 * 
 * @author
 */
public class SpellExpressionFileConverter extends BioFileConverter {
    //
    protected static final Logger LOG = Logger.getLogger(SpellExpressionFileConverter.class);
    private static final String DATASET_TITLE = "Spell Expression Data Dumped from MySQL";
    private static final String DATA_SOURCE_NAME = "Spell";
    private Map<String, Item> genes = new HashMap();
    private Map<String, Item> datasets = new HashMap();
    private Map<String, Item> conditions = new HashMap();
    private Map<String, Item> tags = new HashMap();
    private ArrayList<String> filenames = new ArrayList();
    private static final String TAXON_ID = "4932";
    private Item organism;

    /**
     * Constructor
     *
     * @param writer the ItemWriter used to handle the resultant items
     * @param model  the Model
     */
    public SpellExpressionFileConverter(ItemWriter writer, Model model) throws ObjectStoreException  {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
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
    public void process(Reader reader) throws Exception {
        processDataSets(reader); //process each file
        storeDataSets();
        storeGenes();
    }


    private void processDataSets(Reader preader) throws Exception, ObjectStoreException {

        int count = 0;
        String prevFileNo = "";
        System.out.println("Processing DataSet No. ...");

        Iterator<?> tsvIter;
        try {
            tsvIter = FormattedTextParser.parseTabDelimitedReader(preader);
        } catch (Exception e) {
            throw new BuildException("cannot parse file: " + getCurrentFile(), e);
        }

        while (tsvIter.hasNext()) {

            String[] line = (String[]) tsvIter.next();

            if (line.length < 17) {
                LOG.error("Couldn't process line. Expected 17 cols, but was " + line.length);
                continue;
            }
            count++;

            String geneName = line[0].replace("[convertDB]", "").trim();
            String pubmedID = line[1].trim();
            String fileName = line[2].trim();
            String geoID = line[3].trim();
            String platformID = line[4].trim();
            String channelCount = line[5].trim();
            String datasetName = line[6].trim();
            String description = line[7].trim();
            String numCond = line[8].trim();
            String numGenes = line[9].trim();
            String author = line[10].trim();
            String allAuthors = line[11].trim();
            String title = line[12].trim();
            String journal = line[13].trim();
            String pubYear = line[14].trim();
            String condDesc = line[15].trim();
            String tags = line[16].trim();
            String dataTable = line[17].trim();

            prevFileNo = fileName;
            if(!fileName.equalsIgnoreCase(prevFileNo)) {
                conditions = new HashMap();  //reset conditions for each dataset - filename change
            }
            //System.out.println("gene: "+geneName + "    condDesc: "+ condDesc + " data_table: " + dataTable);

            // create gene first time you see it
            Item gene = getGene(geneName);

            // create dataset first time you see it
            Item dataset = getDataSet(pubmedID, fileName, geoID, platformID,
                    channelCount, datasetName, description, numCond, numGenes,
                    author, allAuthors, title, journal, pubYear, tags);

            // add score to gene - using the condition and dataset info
            getConditionScore(dataset, condDesc, dataTable, geneName);

        }
    }

    /**
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

            if (kwtags.contains("|")) {

                String[] keywords = kwtags.split("\\|");

                for (int i = 0; i < keywords.length; i++) {
                    String kw = keywords[i];

                    Item dtag = tags.get(kw);

                    if (dtag == null) {

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

            } else {
                Item dtag = tags.get(kwtags);

                if (dtag == null) {
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
            //System.out.println("pubmed: "+pubmedID + "    geoID: "+ geoID + " datasetName: " + datasetName);
            datasets.put(fileName, item);
        }

        return item;
    }

    /**
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

            Item dcond = conditions.get(cond);
            if (dcond == null) {

                dcond = createItem("ExpressionCondition");
                dcond.setAttribute("conditionname", cond);
                dcond.setAttribute("ordernumber", String.valueOf(i));
                dcond.setReference("expressiondataset", dataSet.getIdentifier());
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
            if (!condscore.equals("NA")) {
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

}


