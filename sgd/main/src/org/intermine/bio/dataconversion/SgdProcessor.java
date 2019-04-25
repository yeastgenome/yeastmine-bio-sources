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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Queries the sgd oracle database and returns result sets
 * @author Julie Sullivan
 */
public class SgdProcessor
{
	private static final Logger LOG = Logger.getLogger(SgdProcessor.class);  
	private static final String SCHEMA_OWNER = "nex.";

	/**
	 * Return the results of running a query for genes
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getChromosomalFeatureResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT g.feature_no, g.feature_name, g.dbxref_id, g.gene_name, "
             + " f.name_description, g.feature_type, f.headline, f.description, f.qualifier, f.feat_attribute, g.status "
             + " FROM " + SCHEMA_OWNER + "feature g "
             + " left outer join "+ SCHEMA_OWNER + "feat_annotation f on g.feature_no = f.feature_no"
             + " where g.feature_type in (select col_value from "+ SCHEMA_OWNER+"web_metadata "
             + " where application_name = 'Chromosomal Feature Search' "
             + " and col_name = 'FEATURE_TYPE')";*/
		//  

		String query = "select l.dbentity_id, l.systematic_name, d.sgdid, l.gene_name, l.name_description, s.display_name as feature_type,  l.headline,"
				+ " l.description, l.qualifier, d.dbentity_status"
				+ " from nex.locusdbentity l, nex.contig c, nex.dnasequenceannotation a, nex.so s, nex.dbentity d "
				+ " where C.contig_id = A.contig_id "
				+ " and (C.format_name like 'Chromosome_%' OR  C.format_name like '2-micron%') "
				+ " and A.dbentity_id = L.dbentity_id "
				+ " and S.so_id = A.so_id "
				+ " and a.taxonomy_id = 274901 "
				+ " and a.dna_type = 'GENOMIC' "
				+ " and L.dbentity_id = D.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}




	/**
	 * Return the results of running a query for NISS 
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getNISS(Connection connection)
			throws SQLException {

		String query = "select l.dbentity_id, l.systematic_name, d.sgdid, l.gene_name, l.name_description, l.headline,"
				+ " l.description, l.qualifier, d.dbentity_status"
				+ " from nex.locusdbentity l, nex.dbentity d"
				+ " where not_in_s288c = true"
				+ " and L.dbentity_id = D.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}

	/**
	 * Return the results of running a query for genes and chromosomes
	 * @param connection the connection
	 * @return the results
	 * modified to get Active only since the addition of seq_rel and release tables 9/7/11
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getChromosomalFeatureLocationResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT c.feature_no, c.feature_name AS identifier, c.feature_type, "
             +" g.feature_no AS gene_feature_no, g.feature_name, l.strand, l.max_coord, l.min_coord, s.residues, s.seq_length "
             + " FROM " + SCHEMA_OWNER + "feature g "
             + " inner join " + SCHEMA_OWNER + "feat_relationship j on g.feature_no = j.child_feature_no "
             + " inner join " + SCHEMA_OWNER + "feature c on j.parent_feature_no = c.feature_no "
             + " left outer join " + SCHEMA_OWNER + "feat_location l on l.feature_no = g.feature_no"
             + " left outer join "+ SCHEMA_OWNER + "seq s on g.feature_no = s.feature_no"
             + " left outer join "+ SCHEMA_OWNER + "seq_rel sl on s.seq_no = sl.seq_no "
             + " left outer join "+ SCHEMA_OWNER + "release r on r.release_no = sl.release_no "
             + "  where g.feature_type in (select col_value from " + SCHEMA_OWNER + "web_metadata "
             + " where application_name = 'Chromosomal Feature Search' "
             + " and col_name = 'FEATURE_TYPE') "
             + " AND (c.feature_type = 'chromosome' OR c.feature_type = 'plasmid') "
             + " AND l.is_current = 'Y' and s.seq_type = 'genomic' and s.is_current = 'Y'"
             +  " AND  r.sequence_release = (select max(sequence_release) from bud.release)";*/

		String query = "select c.contig_id, c.format_name, s.display_name as feature_type, l.dbentity_id, l.gene_name, a.strand, a.end_index, a.start_index, a.residues, length(a.residues) "
				+ " from nex.locusdbentity l, nex.contig c, nex.dnasequenceannotation a, nex.so s "
				+ " where C.contig_id = A.contig_id "
				+ " and (C.format_name like 'Chromosome_%' OR  C.format_name like '2-micron%')"
				+ " and A.dbentity_id = L.dbentity_id "
				+ " and S.so_id = c.so_id "
				+ " and a.taxonomy_id = 274901 "			 
				+ " and a.dna_type = 'GENOMIC'";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of running a query for genes and chromosomes
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 *
    protected ResultSet getChromosomeLocationResults(Connection connection)
        throws SQLException {

    	String query = "SELECT c.feature_no, c.feature_name AS identifier, c.feature_type, "
             +" g.feature_no AS gene_feature_no "
             + " FROM " + SCHEMA_OWNER + "feature g "
             + " inner join " + SCHEMA_OWNER + "feat_relationship j on g.feature_no = j.child_feature_no "
             + " inner join " + SCHEMA_OWNER + "feature c on j.parent_feature_no = c.feature_no "
             + "  where g.feature_type in ('not physically mapped', 'not in systematic sequence of S288C' ) "
             + " AND (c.feature_type = 'chromosome' OR c.feature_type = 'plasmid') ";

    	LOG.info("executing: " + query);
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(query);
        return res;
    }*/


	/**
	 *
	 * @param connection
	 * @param parent_id
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getChildrenFeatureLocationResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT  f1.feature_no as parent_id, f1.feature_type as parent_type, f2.feature_no as child_id, "
                 	+"f2.feature_type as child_type, f2.feature_name as child_identifier, f2.dbxref_id as child_dbxrefid, "
                    +" fL.max_coord, fL.min_coord, fl.strand, r.feature_name, s2.residues, s2.seq_length, f1.status as parent_status, f2.status as child_status "
                    +" FROM  " + SCHEMA_OWNER + "feature f1," + SCHEMA_OWNER + "feature f2, "
                    + SCHEMA_OWNER + "feat_relationship fr, " + SCHEMA_OWNER + "feat_location fl," + SCHEMA_OWNER + "seq s ," 
                    +  SCHEMA_OWNER + "seq s2, " + SCHEMA_OWNER + "feature r,  " + SCHEMA_OWNER + "seq_rel sl,  " + SCHEMA_OWNER + "release r "
                    +" WHERE  f1.feature_no = fr.parent_feature_no "
                    +" AND fR.child_feature_no = f2.feature_no "
                    +"AND fr.rank IN (2,4) "
                    +"AND f2.feature_no = fl.feature_no "
                    + "AND f2.feature_no = s2.feature_no "
                    +"AND s.seq_no = FL.rootseq_no "
                    +"AND s2.is_current = 'Y' " 
                    +"AND s.is_current = 'Y' " 
                    +"AND r.feature_no = s.feature_no "
                	+ "AND s.seq_no = sl.seq_no "
                	+ "AND r.release_no = sl.release_no "
                	+ "AND r.sequence_release = (select max(sequence_release) from bud.release) "
                    +"AND fl.is_current = 'Y' "
                    +"AND f1.feature_type in (select col_value from "+ SCHEMA_OWNER+"web_metadata "
                    + " where application_name = 'Chromosomal Feature Search' "
                    + " and col_name = 'FEATURE_TYPE') ";*/
		String query = "select a.dbentity_id as parent_id, "
				+ "s.display_name as parent_type, a2.dbentity_id as child_id, a2.display_name as child_type, "
				+ " ld.systematic_name as child_identifier, d2.sgdid as child_sgdid, "
				+ " a2.contig_end_index as child_end_coord, a2.contig_start_index as child_start_coord, "
				+ " a.strand, c.format_name , a2.residues, length(a2.residues) as seq_length, d.dbentity_status as parent_status, d2.dbentity_status as child_status "
				+ " from   nex.contig c, nex.dnasequenceannotation a, nex.dnasubsequence a2, nex.dbentity d, nex.dbentity d2, nex.locusdbentity ld, nex.so s, nex.taxonomy t "
				+ " where c.contig_id = a.contig_id "
				+ " and (C.format_name like 'Chromosome_%' OR  C.format_name like '2-micron%') "
				+ " and    a.annotation_id = a2.annotation_id "
				+ " and    a.dbentity_id = d.dbentity_id "
				+ " and    a.so_id = s.so_id "
				+ " and    a2.dbentity_id = d2.dbentity_id "
				+ " and    d2.dbentity_id = ld.dbentity_id "
				+ " and    a.taxonomy_id = t.taxonomy_id "
				+ " and    t.display_name = 'Saccharomyces cerevisiae S288c'";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}



	/**
	 * Returns the results of running a query for genes and their sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException  if there is a database problem
	 */
	protected ResultSet getChromosomeSequenceResults(Connection connection)
			throws SQLException {
		/*String query =  "SELECT g.feature_no, g.feature_name, g.feature_type, s.residues, s.seq_length "
        + "FROM "+ SCHEMA_OWNER + "feature g, "+ SCHEMA_OWNER + "seq s  " 
        + "WHERE s.seq_no in (select distinct rootseq_no from "+ SCHEMA_OWNER + "feat_location l, "+ SCHEMA_OWNER + "seq s, " 
        + SCHEMA_OWNER + "release r," + SCHEMA_OWNER + "seq_rel sl "       
        + "where s.seq_no = l.rootseq_no "
        + "AND s.feature_no = l.feature_no "
        + "AND s.seq_no = sl.seq_no "
        + "AND sl.release_no = r.release_no "
        + "AND r.sequence_release = (select max(sequence_release) from bud.release) "
        + "AND s.is_current = 'Y') " 
        + "AND g.feature_type in ('plasmid','chromosome') "
        + "AND g.feature_no = s.feature_no";*/

		String query =  "SELECT c.contig_id, c.format_name, s.display_name, c.residues, length(residues) "
				+ "FROM "+ SCHEMA_OWNER + "contig c, "+ SCHEMA_OWNER + "so s  " 
				+ "WHERE s.so_id = c.so_id "
				+ "AND s.display_name in ('chromosome', 'plasmid') "
				+ "AND taxonomy_id = 274901";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}



	/**
	 * Return the results of running a query for protein sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getProteinResults(Connection connection)
			throws SQLException {

		/*String query =  "SELECT g.feature_no, g.dbxref_id, g.feature_name, g.gene_name, s.residues, s.seq_length - 1 " 
    	        + " FROM "+ SCHEMA_OWNER + "feature g " 
    	        + " inner join " + SCHEMA_OWNER + "seq s on g.feature_no = s.feature_no "
    	        + " WHERE s.is_current = 'Y' "
    	        + "AND s.seq_type = 'protein' ";*/

		String query = "select db.dbentity_id, db.sgdid, db.format_name, db.display_name, residues, length(residues) - 1 "
				+ " from nex.dbentity db, nex.locusdbentity ldb, nex.proteinsequenceannotation ps "
				+ " where db.dbentity_id = ldb.dbentity_id "
				+ " and ps.dbentity_id = db.dbentity_id"
				+ " and ps.taxonomy_id = 274901";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of running a query for protein sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getProteinHalfLifeResults(Connection connection)
			throws SQLException {

		String query = "select db.dbentity_id, data_value, data_unit, pmid, rdb.dbentity_id as referencedbentity"
				+ " from nex.dbentity db, nex.proteinexptannotation pea, nex.referencedbentity rdb"
				+ " where pea.dbentity_id = db.dbentity_id"
				+ " and pea.reference_id = rdb.dbentity_id"
				+ " and experiment_type = 'half-life'";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}



	/**
	 * Return the results of running a query for protein sequence info
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getProteinInfoResults(Connection connection)
			throws SQLException {

		String query = "select db.dbentity_id, molecular_weight, n_term_seq, c_term_seq, pi, cai, codon_bias, fop_score,"
				+ " gravy_score, aromaticity_score, aliphatic_index, instability_index, ala, arg, asn, asp, cys, gln,"
				+ " glu, gly, his, ile, leu, lys, met, phe, pro, ser, thr, trp, tyr, val, hydrogen, sulfur, nitrogen,"
				+ " oxygen, carbon, no_cys_ext_coeff, all_cys_ext_coeff"
				+ " from nex.dbentity db, nex.locusdbentity ldb, nex.proteinsequenceannotation ps, nex.proteinsequence_detail psd"
				+ " where db.dbentity_id = ldb.dbentity_id"
				+ " and ps.dbentity_id = db.dbentity_id"
				+ " and psd.annotation_id = ps.annotation_id"
				+ " and ps.taxonomy_id = 274901";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}




	/**
	 * Return the results of running a query for protein sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getProteinAbundanceResults(Connection connection)
			throws SQLException {

		/*String query = "select db.dbentity_id, pea.annotation_id, data_value, data_unit, pmid, rdb.dbentity_id as referencedbentity"
				+ " from nex.dbentity db, nex.proteinexptannotation pea, nex.referencedbentity rdb"
				+ " where pea.dbentity_id = db.dbentity_id"
				+ " and pea.reference_id = rdb.dbentity_id"
				+ " and experiment_type = 'abundance'"
				+ " order by db.dbentity_id";*/
		
		String query = "select db.dbentity_id, pea.annotation_id, data_value, data_unit, rdb2.pmid, rdb1.pmid as parent_pmid, rdb1.dbentity_id as original_referencedbentity, rdb2.dbentity_id as referencedbentity , " 
				+ "median_value, median_abs_dev_value, time_unit, time_value, concentration_value, concentration_unit, " 
				+ "fold_change, g.display_name as process, ef.display_name as media, ec.display_name as assay, chb.display_name as chemical, t.format_name as strain_background "
				+ "from nex.dbentity db " 
				+ "inner join nex.proteinabundanceannotation pea on db.dbentity_id = pea.dbentity_id "
				+ "inner join  nex.referencedbentity rdb1 on pea.reference_id = rdb1.dbentity_id "
				+ "inner join  nex.referencedbentity rdb2 on pea.original_reference_id = rdb2.dbentity_id "
				+ "left join nex.taxonomy t on t.taxonomy_id = pea.taxonomy_id "
				+ "left join nex.go g on g.go_id = pea.process_id "
				+ "left join nex.efo ef on ef.efo_id = pea.media_id " 
				+ "left join nex.eco ec on ec.eco_id = pea.assay_id "
				+ "left join nex.chebi chb on chb.chebi_id = pea.chemical_id ";
				//+ "where db.dbentity_id = 1268219";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of running a query for protein sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getProteinDomainsResults(Connection connection)
			throws SQLException {

		String query = "select db.dbentity_id, start_index, end_index, pd.display_name, interpro_id, description, url_type, date_of_run"
				+ " from nex.dbentity db, nex.proteindomainannotation pda, nex.proteindomain pd, nex.proteindomain_url pdu"
				+ " where pda.dbentity_id = db.dbentity_id"
				+ " and pd.proteindomain_id = pda.proteindomain_id"
				+	" and pdu.proteindomain_id = pd.proteindomain_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of running a query for protein sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getProteinModificationResults(Connection connection)
			throws SQLException {

		String query = "select db.dbentity_id, site_index, site_residue, p.display_name, m.dbentity_id as modifier, pmid, rdb.dbentity_id as referencedbentity"
				+ " from nex.dbentity db"
				+ " inner join nex.posttranslationannotation pda on pda.dbentity_id = db.dbentity_id"
				+ " inner join nex.referencedbentity rdb on rdb.dbentity_id = pda.reference_id"
				+ " inner join nex.psimod p on  p.psimod_id = pda.psimod_id"
				+ " left join nex.dbentity m on m.dbentity_id = pda.modifier_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}



	/**
	 * Return the results of running a query for all publications associated with chromosomal features  
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPubWithFeaturesResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT  f.feature_no AS gene_feature_no , r.reference_no, "
         +" r.pubmed, r.status, r.title, r.volume, r.page, r.year, r.issue,  r.citation, lg.literature_topic, j.abbreviation, r.dbxref_id "
                + "FROM  "+ SCHEMA_OWNER + "reference r,  "+ SCHEMA_OWNER + "feature f,  "
                + SCHEMA_OWNER + "litguide_feat lgf,  "+ SCHEMA_OWNER + "lit_guide lg, "+ SCHEMA_OWNER + "journal j "
                + "WHERE f.feature_no = lgf.feature_no"
                + "   AND lgf.lit_guide_no = lg.lit_guide_no"
                + "   AND lg.reference_no = r.reference_no"
                + " AND j.journal_no  (+) =   r.journal_no"
                +" AND f.feature_type in (select col_value from "+ SCHEMA_OWNER+"web_metadata "
                + " where application_name = 'Chromosomal Feature Search' "
                + " and col_name = 'FEATURE_TYPE')"
                +" ORDER by f.feature_no, r.reference_no";*/
		String query = "select ldb.dbentity_id as featureNo, r.dbentity_id as referenceFeatureNo, r.pmid, r.fulltext_status, r.title, r.volume,"
				+ " r.page, r.year, r.issue, r.citation, la.topic, j.med_abbr, db.sgdid, db.date_created"
				+ " from nex.literatureannotation la"
				+ " inner join nex.referencedbentity r on la.reference_id = r.dbentity_id"
				+ " inner join nex.locusdbentity ldb on la.dbentity_id  = ldb.dbentity_id"
				+ " inner join nex.dbentity db on ldb.dbentity_id = db.dbentity_id"
				+ " left join nex.journal j on j.journal_id = r.journal_id"
				+ " group by ldb.dbentity_id, r.dbentity_id, r.pmid, r.fulltext_status, r.title, r.volume, r.page, r.year, r.issue, r.citation, la.topic, j.med_abbr, db.sgdid, db.date_created"
				+ " order by ldb.dbentity_id, r.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; // f.feature_name = 'HEM2' and
	}

	/**
	 * Return the results of running a query for all publications.  
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPubAllResults(Connection connection)
			throws SQLException {
		/*String query = "SELECT  r.reference_no, "
         +" r.pubmed, r.status, r.title, r.volume, r.page, r.year, r.issue,  r.citation, lg.literature_topic, j.abbreviation, a.abstract, r.dbxref_id "
                + "FROM  "+ SCHEMA_OWNER + "reference r, "+ SCHEMA_OWNER + "lit_guide lg, "+ SCHEMA_OWNER + "journal j, "+ SCHEMA_OWNER + "abstract a  "
                + "WHERE  lg.reference_no = r.reference_no"
                + " AND j.journal_no  (+) =   r.journal_no"
                + " AND a.reference_no (+) = r.reference_no"
                +" ORDER by r.reference_no";*/

		String query = "select r.dbentity_id, r.pmid, r.fulltext_status, r.title, r.volume, r.page, r.year, r.issue,"
				+	" r.citation, la.topic, j.med_abbr, rd.text, db.sgdid, db.date_created"
				+ " from nex.dbentity db"
				+ " inner join nex.referencedbentity r on db.dbentity_id = r.dbentity_id"
				+ " left join nex.journal j on j.journal_id = r.journal_id"
				+ " left join nex.referencedocument rd on  r.dbentity_id = rd.reference_id"
				+ " left join nex.literatureannotation la on la.reference_id = r.dbentity_id"
				+ " and rd.document_type = 'Abstract'"
				+ " order by r.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; 
	}


	/**
	 * Return the results of running a query for all publications.  
	 * TODO only retreive publications for phenotype_annot_no
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPubForPhenotype(Connection connection)
			throws SQLException {
		/*String query = "SELECT  rl.primary_key AS pheno_annot_no , r.reference_no, "
         +" r.pubmed, r.status, r.title, r.volume, r.page, r.year, r.issue,  r.citation, j.abbreviation, r.dbxref_id "
                + "FROM  "+ SCHEMA_OWNER + "reference r,  "+ SCHEMA_OWNER + "ref_link rl,  "+ SCHEMA_OWNER + "journal j "
                + "WHERE  rl.reference_no = r.reference_no"
                + "   AND rl.tab_name = 'PHENO_ANNOTATION'"
                + " AND j.journal_no  (+) =   r.journal_no"
                +" ORDER by rl.primary_key, r.reference_no";*/

		String query = "select annotation_id, reference_id, pmid, fulltext_status, rdb.title, volume, page, year, issue, citation, med_abbr, db.sgdid, db.date_created"
				+ " from nex.phenotypeannotation pa, nex.referencedbentity rdb, nex.journal j, nex.dbentity db"
				+ " where pa.reference_id = rdb.dbentity_id"
				+" and rdb.journal_id = j.journal_id"
				+ " and rdb.dbentity_id = db.dbentity_id"
				+ " order by annotation_id, reference_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; 
	}
	/**
	 * Return the results of running a query for phenotype summaries
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPhenotypeSummary(Connection connection)
			throws SQLException {

		String query = "select distinct(text), db.dbentity_id"
				+ " from nex.phenotypeannotation pa, nex.dbentity db,  nex.locussummary ls"
				+ " where pa.dbentity_id = db.dbentity_id"
				+ " and db.dbentity_id  = ls.locus_id"
				+ " and summary_type = 'Phenotype'"
				+ " group by text, db.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; 
	}

	/**
	 * Return the results of running a query for phenotype summaries
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getFunctionSummary(Connection connection)
			throws SQLException {

		String query = "select distinct(text), db.dbentity_id"
				+ " from nex.goannotation ga, nex.dbentity db,  nex.locussummary ls"
				+ " where ga.dbentity_id = db.dbentity_id"
				+ " and db.dbentity_id  = ls.locus_id"
				+ " and summary_type = 'Function'"
				+ " group by text, db.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; 
	}

	/**
	 * Return the results of running a query for phenotype summaries
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getGeneSummary(Connection connection)
			throws SQLException {

		String query = "select distinct(text), db.dbentity_id"
				+ " from nex.locusdbentity ldb, nex.dbentity db,  nex.locussummary ls"
				+ " where ldb.dbentity_id = db.dbentity_id"
				+ " and db.dbentity_id  = ls.locus_id"
				+ " and summary_type = 'Gene'"
				+ " group by text, db.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; 
	}


	/**
	 * Return the results of running a query for phenotype summaries
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getRegulationSummary(Connection connection)
			throws SQLException {

		String query = "select distinct(text), db.dbentity_id, reference_id, pmid"
				+ " from nex.locusdbentity ldb, nex.dbentity db,  nex.locussummary ls, nex.locussummary_reference lsr, nex.referencedbentity rdb"
				+ " where ldb.dbentity_id = db.dbentity_id"
				+ " and db.dbentity_id  = ls.locus_id"
				+ " and lsr.summary_id = ls.summary_id"
				+ " and lsr.reference_id = rdb.dbentity_id"
				+ " and summary_type = 'Regulation'"
				+ " group by text, db.dbentity_id, reference_id, pmid";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res; 
	}

	/**
	 * Return the results of running a query for CDSs and their sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPhysicalInteractionResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT interaction_no, feature_a, feature_b, experiment_type, interaction_type, "
        	+" action, i.source, annotation_type, modification, phenotype, r.citation, i.pubmed, r.title, r.volume, r.page, r.year, r.issue, j.abbreviation, "
        	+ " r.reference_no, substr(r.citation, 0, (instr(r.citation, ')',1,1) )) as first_author, r.dbxref_id  "
            + " FROM "+ SCHEMA_OWNER + "interaction_mv i, "+ SCHEMA_OWNER + "reference r, "+ SCHEMA_OWNER + "journal j"
            + " WHERE i.pubmed = r.pubmed and r.journal_no = j.journal_no ORDER by feature_a";*/
		
		String query = "select annotation_id, dbentity1_id, dbentity2_id, biogrid_experimental_system, bait_hit, s.display_name, annotation_type, psi.display_name as modification,"
				+ " citation, pmid, rdb.title, volume, page, year, issue, med_abbr, reference_id, substring(citation, 0, position( ')' in citation)+1) as first_author, db.sgdid, "
				+ " pa.description as note"
				+ " from nex.physinteractionannotation pa"
				+ " left join nex.psimod psi on pa.psimod_id = psi.psimod_id" 
				+ " inner join nex.source s on s.source_id = pa.source_id"
				+ " inner join  nex.referencedbentity rdb on pa.reference_id = rdb.dbentity_id"
				+ " left join nex.journal j on rdb.journal_id = j.journal_id"
				+ " inner join nex.dbentity db on  db.dbentity_id = rdb.dbentity_id";
		


		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}

	/**
	 * Return the results of running a query for CDSs and their sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getGeneticInteractionResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT interaction_no, feature_a, feature_b, experiment_type, interaction_type, "
        	+" action, i.source, annotation_type, modification, phenotype, r.citation, i.pubmed, r.title, r.volume, r.page, r.year, r.issue, j.abbreviation, "
        	+ " r.reference_no, substr(r.citation, 0, (instr(r.citation, ')',1,1) )) as first_author, r.dbxref_id  "
            + " FROM "+ SCHEMA_OWNER + "interaction_mv i, "+ SCHEMA_OWNER + "reference r, "+ SCHEMA_OWNER + "journal j"
            + " WHERE i.pubmed = r.pubmed and r.journal_no = j.journal_no ORDER by feature_a";*/

		String query = "select annotation_id, dbentity1_id, dbentity2_id, biogrid_experimental_system, p.display_name as phenotype, bait_hit, s.display_name as source, annotation_type,"
				+ " citation, pmid, rdb.title, volume, page, year, issue, med_abbr, reference_id, substring(citation, 0, position( ')' in citation)+1) as first_author, db.sgdid, "
				+ " ga.description as note"
				+ " from nex.geninteractionannotation ga"
				+ " left join nex.phenotype p on p.phenotype_id = ga.phenotype_id"
				+ " inner join nex.source s on s.source_id = ga.source_id"
				+ " inner join nex.referencedbentity rdb on rdb.dbentity_id = ga.reference_id"
				+ " left join nex.journal j on rdb.journal_id = j.journal_id"
				+ " inner join nex.dbentity db on  db.dbentity_id = rdb.dbentity_id";

		LOG.info("executing: " + query);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of running a query for CDSs and their sequences
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPhenotypeResults(Connection connection)
			throws SQLException {

		/*String query = "SELECT  me.feature_no, pheno_annotations.pheno_annotation_no, "
        + " phenotype_no.experiment_type,  experiment_no.experiment_comment, phenotype_no.mutant_type, phenotype_no.qualifier, phenotype_no.observable, "
        + " expt_property_no.property_type, expt_property_no.property_value, "
        + " expt_property_no.property_description, me.feature_type "

        + " FROM BUD.feature me "

        + " INNER JOIN BUD.pheno_annotation pheno_annotations ON  pheno_annotations.feature_no = me.feature_no"
        + " INNER JOIN BUD.phenotype phenotype_no ON phenotype_no.phenotype_no = pheno_annotations.phenotype_no "
        + " LEFT JOIN BUD.experiment experiment_no ON experiment_no.experiment_no = pheno_annotations.experiment_no "
        + " LEFT JOIN BUD.expt_exptprop expt_exptprops ON expt_exptprops.experiment_no = pheno_annotations.experiment_no "
        + " LEFT JOIN BUD.expt_property expt_property_no ON expt_property_no.expt_property_no = expt_exptprops.expt_property_no " 

        + " ORDER BY me.feature_no, pheno_annotations.pheno_annotation_no";*/

		String query = "  select db.dbentity_id, pa.annotation_id, pac.group_id, p.display_name as phenotype, t.format_name as strain_name, pa.details,"
				+ " pa.experiment_comment, pa.allele_comment, pa.reporter_comment, a1.display_name as experiment, a2.display_name as mutant,"
				+ " al.display_name as allele, rp.display_name as reporter, o.display_name as assay, rdb.pmid, rdb.dbentity_id as refNo,"
				+ " array_agg(condition_class) as condclass,"
				+ " array_agg(condition_name) as condname , array_agg(condition_value) as condvalue,"
				+ " array_agg(condition_unit) as condunit"
				+ " from nex.phenotypeannotation pa"
				+ " inner join nex.dbentity db on  db.dbentity_id = pa.dbentity_id"
				+ " inner join nex.phenotype p on pa.phenotype_id = p.phenotype_id"
				+ " inner join nex.referencedbentity rdb on pa.reference_id = rdb.dbentity_id"
				+ " inner join nex.taxonomy t on  pa.taxonomy_id = t.taxonomy_id"
				+ " left join nex.phenotypeannotation_cond pac on pac.annotation_id = pa.annotation_id"
				+ " left join nex.apo a1 on pa.experiment_id = a1.apo_id"
				+ " left join nex.apo a2 on pa.mutant_id = a2.apo_id"
				+ " left join nex.allele al on al.allele_id = pa.allele_id"
				+ " left join nex.reporter rp on rp.reporter_id = pa.reporter_id"
				+ " left join nex.obi o on pa.assay_id = o.obi_id"
				//+ " where db.dbentity_id in (1267638)"
				+ " group by  db.dbentity_id, pa.annotation_id, pac.group_id, p.display_name, t.format_name, pa.details,"
				+ " pa.experiment_comment, pa.allele_comment, pa.reporter_comment, a1.display_name, a2.display_name,"
				+ " al.display_name, rp.display_name, o.display_name, rdb.pmid, rdb.dbentity_id"
				+ " order by db.dbentity_id, pa.annotation_id, pac.group_id ";
	
		LOG.info("executing: " + query);        
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}
	

	/**
	 * Return the results of getting aliases
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getAliases(Connection connection)
			throws SQLException {

		/*String query = "SELECT f.feature_no as gene_feature_no, alias_name, alias_type "
        + "FROM "+ SCHEMA_OWNER + "alias a, "+ SCHEMA_OWNER + "feat_alias fa,"+ SCHEMA_OWNER + "feature f "
        + " WHERE f.feature_type in (select col_value from "+ SCHEMA_OWNER+"web_metadata "
        + " WHERE application_name = 'Chromosomal Feature Search' "
        + " AND col_name = 'FEATURE_TYPE') "
        + "AND fa.alias_no = a.alias_no "
        + "AND fa.feature_no = f.feature_no order by f.feature_no asc";*/

		String query = "select  l.dbentity_id, la.display_name, alias_type "
				+ "from nex.locusdbentity l, nex.locus_alias la, nex.contig c, nex.dnasequenceannotation a, nex.so s, nex.dbentity d "
				+ "where l.dbentity_id = a.dbentity_id "
				+ "and l.dbentity_id = la.locus_id "
				+ "and c.contig_id = a.contig_id "
				+ "and c.so_id = s.so_id "
				+ "and s.display_name in ('chromosome', 'plasmid')"
				+ "and a.dna_type = 'GENOMIC' "
				+ "and a.taxonomy_id = 274901 "
				+ "and d.dbentity_id = l.dbentity_id "
				+ "and d.dbentity_status = 'Active' "
				+ "and alias_type in ('Uniform', 'Non-uniform', 'Retired name', 'NCBI protein name')";


		LOG.info("executing: " + query);        
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of getting paralogs
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getParalogs(Connection connection)
			throws SQLException {

		/*String query = "SELECT parent_feature_no, child_feature_no, r.reference_no "
        + "FROM "+ SCHEMA_OWNER + "feat_relationship fr, "+ SCHEMA_OWNER + "ref_link fl, "+ SCHEMA_OWNER + "reference  r "
        + " WHERE relationship_type = 'pair' "
        + " AND fl.tab_name = 'FEAT_RELATIONSHIP' "
        + " AND fr.feat_relationship_no = fl.primary_key "
        + " AND fl.reference_no = r.reference_no";*/
		String query = "SELECT parent_id, child_id, reference_id "
				+ "from nex.locus_relation lr, nex.locusrelation_reference lrr "
				+ " where ro_id = 169738 "
				+ " and lr.relation_id = lrr.relation_id";

		LOG.info("executing: " + query);        
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results of getting cross-references from dbxref
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getCrossReferences(Connection connection)
			throws SQLException {

		/*String query = "select f.feature_no, dbx.dbxref_id,  dbx.source, dbx.dbxref_type"
    		+ " FROM bud.feature f, bud.dbxref dbx, bud.dbxref_feat df, bud.dbxref_url du, bud.url u"
    		+ " where f.feature_no = df.feature_no AND df.dbxref_no = dbx.dbxref_no AND dbx.dbxref_no = du.dbxref_no AND du.url_no = u.url_no"
    		+ " AND f.feature_type in (select col_value from bud.web_metadata "
    		+ " WHERE application_name = 'Chromosomal Feature Search' "
    		+ " AND col_name = 'FEATURE_TYPE') "
    		+ " and dbx.source != 'SGD'"  //in ('EBI', 'GenBank/EMBL/DDBJ', 'IUBMB', 'NCBI', 'TCDB', 'RNAcentral') "
    		+ " group by f.feature_no, dbx.dbxref_id,  dbx.source, dbx.dbxref_type"
    		+ " order by f.feature_no asc";*/

		String query = "select l.dbentity_id, la.display_name, so.display_name, alias_type "
				+ "from nex.locusdbentity l, nex.locus_alias la, nex.contig c, nex.dnasequenceannotation a, nex.so s, nex.dbentity d, nex.source so "
				+ "where l.dbentity_id = a.dbentity_id "
				+ "and l.dbentity_id = la.locus_id "
				+ "and c.contig_id = a.contig_id "
				+ "and c.so_id = s.so_id "
				+ "and so.source_id = la.source_id "
				+ "and s.display_name in ('chromosome', 'plasmid') "
				+ "and a.dna_type = 'GENOMIC' "
				+ "and a.taxonomy_id = 274901 "
				+ "and d.dbentity_id = l.dbentity_id "
				+ "and d.dbentity_status = 'Active' "
				+ "and alias_type NOT in ('Uniform', 'Non-uniform', 'Retired name', 'NCBI protein name')";

		LOG.info("executing: " + query);        
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}

	/**
	 * Return the results of getting cross-references from dbxref
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database problem
	 */
	protected ResultSet getPathways(Connection connection)
			throws SQLException {

		/*String query = "select f.feature_no, dbx.dbxref_id, dbxref_name "
    		+ " FROM bud.feature f, bud.dbxref dbx, bud.dbxref_feat df, bud.dbxref_url du, bud.url u"
    		+ " where f.feature_no = df.feature_no AND df.dbxref_no = dbx.dbxref_no AND dbx.dbxref_no = du.dbxref_no AND du.url_no = u.url_no"
    		+ " AND f.feature_type in (select col_value from bud.web_metadata "
    		+ " WHERE application_name = 'Chromosomal Feature Search' "
    		+ " AND col_name = 'FEATURE_TYPE') "
    		+ " and dbx.source = 'MetaCyc' "
    		+ " order by f.feature_no asc";*/
		String query = "select distinct db.dbentity_id, biocyc_id, db2.display_name, ps.summary_type, ps.text "
				+ " from nex.dbentity db, nex.pathwaydbentity pdf, nex.pathwayannotation pa, nex.dbentity db2, nex.pathwaysummary ps "
				+ " where db.dbentity_id = pa.dbentity_id "
				+ " and ps.pathway_id = pdf.dbentity_id "
				+ " and pa.pathway_id = pdf.dbentity_id "
				+ " and pdf.dbentity_id = db2.dbentity_id " 
				+ " order by 1";

		LOG.info("executing: " + query);        
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


	/**
	 * Return the results for regulation info
	 * @param connection the connection
	 * @return the results
	 * @throws SQLException if there is a database proble
	 */
	protected ResultSet getRegulationData(Connection connection)
			throws SQLException {

		String query = "select target_id, regulator_id, regulator_type, regulation_type, direction, annotation_type, pmid,"
				+ " rdb.dbentity_id as refNo, e.format_name, g.display_name as happens_during, s.display_name as source, t.format_name as strain_background"
				+ " from nex.dbentity db"
				+ " inner join nex.regulationannotation ra on db.dbentity_id = ra.regulator_id"
				+ " inner join nex.referencedbentity rdb on rdb.dbentity_id = ra.reference_id"
				+ " inner join nex.dbentity db2 on ra.target_id = db2.dbentity_id"
				+ " left join nex.eco e on e.eco_id = ra.eco_id"
				+ " left join nex.go g on g.go_id = ra.happens_during"
				+ " left join nex.taxonomy t on t.taxonomy_id = ra.taxonomy_id"
				+ " left join nex.source s on s.source_id = ra.source_id"
				+ " order by 1";

		LOG.info("executing: " + query);        
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(query);
		return res;
	}


}
