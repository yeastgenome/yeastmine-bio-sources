package org.intermine.model.bio;

public interface GOEvidence extends org.intermine.model.InterMineObject
{
    public org.intermine.model.bio.GOEvidenceCode getCode();
    public void setCode(final org.intermine.model.bio.GOEvidenceCode code);
    public void proxyCode(final org.intermine.objectstore.proxy.ProxyReference code);
    public org.intermine.model.InterMineObject proxGetCode();

    public java.util.Set<org.intermine.model.bio.BioEntity> getWith();
    public void setWith(final java.util.Set<org.intermine.model.bio.BioEntity> with);
    public void addWith(final org.intermine.model.bio.BioEntity arg);

    public java.util.Set<org.intermine.model.bio.Publication> getPublications();
    public void setPublications(final java.util.Set<org.intermine.model.bio.Publication> publications);
    public void addPublications(final org.intermine.model.bio.Publication arg);

}
