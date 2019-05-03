package org.intermine.bio.dataconversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;


public class DiseaseProcessor {
	
	private static final Logger LOG = Logger.getLogger(DiseaseProcessor.class);  
	
    /**
     * Return the results of running a query for genes
     * @param connection the connection
     * @return the results
     * @throws SQLException if there is a database problem
     */
	protected ResultSet getDiseaseAnnotations(Connection connection) throws SQLException {
		
		String query = "select t.taxid, sgdid, gene_name, r.display_name as relation_type, doid, " 
				+ " dse.dbxref_id as hgnc_id, ea.display_name as evidence_code, da.annotation_type, dse.evidence_type, "  
				+ " pmid, da.date_assigned, da.created_by, s.display_name as source "
				+ " from nex.disease d, nex.diseaseannotation da, nex.taxonomy t, nex.dbentity db, nex.ro r, "
				+ " nex.eco_alias ea, nex.referencedbentity rdb, nex.locusdbentity ldb, nex.diseasesupportingevidence dse, nex.source s " 
				+ "where d.disease_id = da.disease_id " 
				+ "and da.annotation_id = dse.annotation_id "
				+ "and t.taxonomy_id = da.taxonomy_id " 
				+ "and db.dbentity_id = da.dbentity_id " 
				+ "and da.association_type = r.ro_id " 
				+ "and da.eco_id = ea.eco_id " 
				+ "and ea.display_name in ('ISS', 'IGI', 'IMP') "
				+ "and da.reference_id = rdb.dbentity_id " 
				+ "and db.dbentity_id = ldb.dbentity_id "
				+ "and da.source_id = s.source_id " 
				+ "group by taxid, sgdid, gene_name, gene_name, r.display_name, doid, dse.dbxref_id, ea.display_name,  da.annotation_type, dse.evidence_type, pmid, da.date_assigned, da.created_by, s.display_name " 
				+ "order by sgdid, doid, dbxref_id, pmid";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}
	

	

}
