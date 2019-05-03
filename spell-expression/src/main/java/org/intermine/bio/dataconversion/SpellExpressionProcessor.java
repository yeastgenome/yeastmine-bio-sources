package org.intermine.bio.dataconversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;


public class SpellExpressionProcessor {
	
	private static final Logger LOG = Logger.getLogger(SpellExpressionProcessor.class);  
	
    /**
     * Return the results of running a query for genes
     * @param connection the connection
     * @return the results
     * @throws SQLException if there is a database problem
     */
	protected ResultSet getDataSets(Connection connection, String filename) throws SQLException {

		String query = "select pubmedID, filename, geoID, platformID, channelCount, d.name, description, num_conds, "
				+ "num_genes, author, all_authors, title, journal, pub_year, cond_descs, tags, g.name, data_table "
				+ " from datasets d, exprs e, genes g "
				+ " where d.id = e.dsetID and g.id = e.geneID and filename = '"+filename+"'";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}
	
    /**
     * 
     * @param connection
     * @return
     * @throws SQLException
     */
	
	protected ResultSet getDataSetFileNames(Connection connection) throws SQLException {

		String query = "select filename from datasets where filename != 'GSE12822_setA_family.pcl' ";
		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}
    
	

}
