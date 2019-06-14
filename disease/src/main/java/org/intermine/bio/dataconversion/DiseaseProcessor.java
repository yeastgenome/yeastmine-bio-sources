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
				+"dse.dbxref_id as hgnc_id, ea.display_name as evidence_code, da.annotation_type, dse.evidence_type, pmid, da.date_assigned, da.created_by, s.display_name as source "
				+"from nex.diseaseannotation da "
				+"inner join nex.disease d on d.disease_id = da.disease_id "
				+"left join nex.diseasesupportingevidence dse on da.annotation_id = dse.annotation_id "
				+"inner join nex.taxonomy t on t.taxonomy_id = da.taxonomy_id "
				+"inner join  nex.dbentity db on db.dbentity_id = da.dbentity_id "
				+"inner join nex.locusdbentity ldb on db.dbentity_id = ldb.dbentity_id "
				+"inner join nex.ro r on da.association_type = r.ro_id "
				+"inner join nex.eco_alias ea on da.eco_id = ea.eco_id "
				+"inner join nex.referencedbentity rdb on da.reference_id = rdb.dbentity_id "
				+"inner join nex.source s on da.source_id = s.source_id "
				+"and ea.display_name in ('ISS', 'IGI', 'IMP') "
				+"group by taxid, sgdid, gene_name, gene_name, r.display_name, doid, dse.dbxref_id, ea.display_name,  da.annotation_type, dse.evidence_type, pmid, da.date_assigned, da.created_by, s.display_name "
				+"order by sgdid, doid, dbxref_id, pmid";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}
	

	

}
