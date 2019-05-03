package org.intermine.model.bio;

import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.intermine.NotXmlParser;
import org.intermine.objectstore.intermine.NotXmlRenderer;
import org.intermine.objectstore.proxy.ProxyCollection;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.model.StringConstructor;
import org.intermine.metadata.TypeUtil;
import org.intermine.util.DynamicUtil;
import org.intermine.model.ShadowClass;

public class GOEvidenceShadow implements GOEvidence, ShadowClass
{
    public static final Class<GOEvidence> shadowOf = GOEvidence.class;
    // Ref: org.intermine.model.bio.GOEvidence.code
    protected org.intermine.model.InterMineObject code;
    public org.intermine.model.bio.GOEvidenceCode getCode() { if (code instanceof org.intermine.objectstore.proxy.ProxyReference) { return ((org.intermine.model.bio.GOEvidenceCode) ((org.intermine.objectstore.proxy.ProxyReference) code).getObject()); }; return (org.intermine.model.bio.GOEvidenceCode) code; }
    public void setCode(final org.intermine.model.bio.GOEvidenceCode code) { this.code = code; }
    public void proxyCode(final org.intermine.objectstore.proxy.ProxyReference code) { this.code = code; }
    public org.intermine.model.InterMineObject proxGetCode() { return code; }

    // Col: org.intermine.model.bio.GOEvidence.with
    protected java.util.Set<org.intermine.model.bio.BioEntity> with = new java.util.HashSet<org.intermine.model.bio.BioEntity>();
    public java.util.Set<org.intermine.model.bio.BioEntity> getWith() { return with; }
    public void setWith(final java.util.Set<org.intermine.model.bio.BioEntity> with) { this.with = with; }
    public void addWith(final org.intermine.model.bio.BioEntity arg) { with.add(arg); }

    // Col: org.intermine.model.bio.GOEvidence.publications
    protected java.util.Set<org.intermine.model.bio.Publication> publications = new java.util.HashSet<org.intermine.model.bio.Publication>();
    public java.util.Set<org.intermine.model.bio.Publication> getPublications() { return publications; }
    public void setPublications(final java.util.Set<org.intermine.model.bio.Publication> publications) { this.publications = publications; }
    public void addPublications(final org.intermine.model.bio.Publication arg) { publications.add(arg); }

    // Attr: org.intermine.model.InterMineObject.id
    protected java.lang.Integer id;
    public java.lang.Integer getId() { return id; }
    public void setId(final java.lang.Integer id) { this.id = id; }

    @Override public boolean equals(Object o) { return (o instanceof GOEvidence && id != null) ? id.equals(((GOEvidence)o).getId()) : this == o; }
    @Override public int hashCode() { return (id != null) ? id.hashCode() : super.hashCode(); }
    @Override public String toString() { return "GOEvidence [code=" + (code == null ? "null" : (code.getId() == null ? "no id" : code.getId().toString())) + ", id=" + id + "]"; }
    public Object getFieldValue(final String fieldName) throws IllegalAccessException {
        if ("code".equals(fieldName)) {
            if (code instanceof ProxyReference) {
                return ((ProxyReference) code).getObject();
            } else {
                return code;
            }
        }
        if ("with".equals(fieldName)) {
            return with;
        }
        if ("publications".equals(fieldName)) {
            return publications;
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.bio.GOEvidence.class.equals(getClass())) {
            return TypeUtil.getFieldValue(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public Object getFieldProxy(final String fieldName) throws IllegalAccessException {
        if ("code".equals(fieldName)) {
            return code;
        }
        if ("with".equals(fieldName)) {
            return with;
        }
        if ("publications".equals(fieldName)) {
            return publications;
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.bio.GOEvidence.class.equals(getClass())) {
            return TypeUtil.getFieldProxy(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public void setFieldValue(final String fieldName, final Object value) {
        if ("code".equals(fieldName)) {
            code = (org.intermine.model.InterMineObject) value;
        } else if ("with".equals(fieldName)) {
            with = (java.util.Set) value;
        } else if ("publications".equals(fieldName)) {
            publications = (java.util.Set) value;
        } else if ("id".equals(fieldName)) {
            id = (java.lang.Integer) value;
        } else {
            if (!org.intermine.model.bio.GOEvidence.class.equals(getClass())) {
                DynamicUtil.setFieldValue(this, fieldName, value);
                return;
            }
            throw new IllegalArgumentException("Unknown field " + fieldName);
        }
    }
    public Class<?> getFieldType(final String fieldName) {
        if ("code".equals(fieldName)) {
            return org.intermine.model.bio.GOEvidenceCode.class;
        }
        if ("with".equals(fieldName)) {
            return java.util.Set.class;
        }
        if ("publications".equals(fieldName)) {
            return java.util.Set.class;
        }
        if ("id".equals(fieldName)) {
            return java.lang.Integer.class;
        }
        if (!org.intermine.model.bio.GOEvidence.class.equals(getClass())) {
            return TypeUtil.getFieldType(org.intermine.model.bio.GOEvidence.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public StringConstructor getoBJECT() {
        if (!org.intermine.model.bio.GOEvidenceShadow.class.equals(getClass())) {
            return NotXmlRenderer.render(this);
        }
        StringConstructor sb = new StringConstructor();
        sb.append("$_^org.intermine.model.bio.GOEvidence");
        if (code != null) {
            sb.append("$_^rcode$_^").append(code.getId());
        }
        if (id != null) {
            sb.append("$_^aid$_^").append(id);
        }
        return sb;
    }
    public void setoBJECT(String notXml, ObjectStore os) {
        setoBJECT(NotXmlParser.SPLITTER.split(notXml), os);
    }
    public void setoBJECT(final String[] notXml, final ObjectStore os) {
        if (!org.intermine.model.bio.GOEvidenceShadow.class.equals(getClass())) {
            throw new IllegalStateException("Class " + getClass().getName() + " does not match code (org.intermine.model.bio.GOEvidence)");
        }
        for (int i = 2; i < notXml.length;) {
            int startI = i;
            if ((i < notXml.length) &&"rcode".equals(notXml[i])) {
                i++;
                code = new ProxyReference(os, Integer.valueOf(notXml[i]), org.intermine.model.bio.GOEvidenceCode.class);
                i++;
            };
            if ((i < notXml.length) && "aid".equals(notXml[i])) {
                i++;
                id = Integer.valueOf(notXml[i]);
                i++;
            }
            if (startI == i) {
                throw new IllegalArgumentException("Unknown field " + notXml[i]);
            }
        }
        with = new ProxyCollection<org.intermine.model.bio.BioEntity>(os, this, "with", org.intermine.model.bio.BioEntity.class);
        publications = new ProxyCollection<org.intermine.model.bio.Publication>(os, this, "publications", org.intermine.model.bio.Publication.class);
    }
    public void addCollectionElement(final String fieldName, final org.intermine.model.InterMineObject element) {
        if ("with".equals(fieldName)) {
            with.add((org.intermine.model.bio.BioEntity) element);
        } else if ("publications".equals(fieldName)) {
            publications.add((org.intermine.model.bio.Publication) element);
        } else {
            if (!org.intermine.model.bio.GOEvidence.class.equals(getClass())) {
                TypeUtil.addCollectionElement(this, fieldName, element);
                return;
            }
            throw new IllegalArgumentException("Unknown collection " + fieldName);
        }
    }
    public Class<?> getElementType(final String fieldName) {
        if ("with".equals(fieldName)) {
            return org.intermine.model.bio.BioEntity.class;
        }
        if ("publications".equals(fieldName)) {
            return org.intermine.model.bio.Publication.class;
        }
        if (!org.intermine.model.bio.GOEvidence.class.equals(getClass())) {
            return TypeUtil.getElementType(org.intermine.model.bio.GOEvidence.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
}
