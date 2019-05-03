package org.intermine.model.bio;

import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.intermine.NotXmlParser;
import org.intermine.objectstore.intermine.NotXmlRenderer;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.model.StringConstructor;
import org.intermine.metadata.TypeUtil;
import org.intermine.util.DynamicUtil;
import org.intermine.model.ShadowClass;

public class CrossReferenceShadow implements CrossReference, ShadowClass
{
    public static final Class<CrossReference> shadowOf = CrossReference.class;
    // Attr: org.intermine.model.bio.CrossReference.dbxreftype
    protected java.lang.String dbxreftype;
    public java.lang.String getDbxreftype() { return dbxreftype; }
    public void setDbxreftype(final java.lang.String dbxreftype) { this.dbxreftype = dbxreftype; }

    // Attr: org.intermine.model.bio.CrossReference.identifier
    protected java.lang.String identifier;
    public java.lang.String getIdentifier() { return identifier; }
    public void setIdentifier(final java.lang.String identifier) { this.identifier = identifier; }

    // Attr: org.intermine.model.bio.CrossReference.url
    protected java.lang.String url;
    public java.lang.String getUrl() { return url; }
    public void setUrl(final java.lang.String url) { this.url = url; }

    // Ref: org.intermine.model.bio.CrossReference.source
    protected org.intermine.model.InterMineObject source;
    public org.intermine.model.bio.DataSource getSource() { if (source instanceof org.intermine.objectstore.proxy.ProxyReference) { return ((org.intermine.model.bio.DataSource) ((org.intermine.objectstore.proxy.ProxyReference) source).getObject()); }; return (org.intermine.model.bio.DataSource) source; }
    public void setSource(final org.intermine.model.bio.DataSource source) { this.source = source; }
    public void proxySource(final org.intermine.objectstore.proxy.ProxyReference source) { this.source = source; }
    public org.intermine.model.InterMineObject proxGetSource() { return source; }

    // Ref: org.intermine.model.bio.CrossReference.subject
    protected org.intermine.model.InterMineObject subject;
    public org.intermine.model.bio.BioEntity getSubject() { if (subject instanceof org.intermine.objectstore.proxy.ProxyReference) { return ((org.intermine.model.bio.BioEntity) ((org.intermine.objectstore.proxy.ProxyReference) subject).getObject()); }; return (org.intermine.model.bio.BioEntity) subject; }
    public void setSubject(final org.intermine.model.bio.BioEntity subject) { this.subject = subject; }
    public void proxySubject(final org.intermine.objectstore.proxy.ProxyReference subject) { this.subject = subject; }
    public org.intermine.model.InterMineObject proxGetSubject() { return subject; }

    // Attr: org.intermine.model.InterMineObject.id
    protected java.lang.Integer id;
    public java.lang.Integer getId() { return id; }
    public void setId(final java.lang.Integer id) { this.id = id; }

    @Override public boolean equals(Object o) { return (o instanceof CrossReference && id != null) ? id.equals(((CrossReference)o).getId()) : this == o; }
    @Override public int hashCode() { return (id != null) ? id.hashCode() : super.hashCode(); }
    @Override public String toString() { return "CrossReference [dbxreftype=" + (dbxreftype == null ? "null" : "\"" + dbxreftype + "\"") + ", id=" + id + ", identifier=" + (identifier == null ? "null" : "\"" + identifier + "\"") + ", source=" + (source == null ? "null" : (source.getId() == null ? "no id" : source.getId().toString())) + ", subject=" + (subject == null ? "null" : (subject.getId() == null ? "no id" : subject.getId().toString())) + ", url=" + (url == null ? "null" : "\"" + url + "\"") + "]"; }
    public Object getFieldValue(final String fieldName) throws IllegalAccessException {
        if ("dbxreftype".equals(fieldName)) {
            return dbxreftype;
        }
        if ("identifier".equals(fieldName)) {
            return identifier;
        }
        if ("url".equals(fieldName)) {
            return url;
        }
        if ("source".equals(fieldName)) {
            if (source instanceof ProxyReference) {
                return ((ProxyReference) source).getObject();
            } else {
                return source;
            }
        }
        if ("subject".equals(fieldName)) {
            if (subject instanceof ProxyReference) {
                return ((ProxyReference) subject).getObject();
            } else {
                return subject;
            }
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.bio.CrossReference.class.equals(getClass())) {
            return TypeUtil.getFieldValue(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public Object getFieldProxy(final String fieldName) throws IllegalAccessException {
        if ("dbxreftype".equals(fieldName)) {
            return dbxreftype;
        }
        if ("identifier".equals(fieldName)) {
            return identifier;
        }
        if ("url".equals(fieldName)) {
            return url;
        }
        if ("source".equals(fieldName)) {
            return source;
        }
        if ("subject".equals(fieldName)) {
            return subject;
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.bio.CrossReference.class.equals(getClass())) {
            return TypeUtil.getFieldProxy(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public void setFieldValue(final String fieldName, final Object value) {
        if ("dbxreftype".equals(fieldName)) {
            dbxreftype = (java.lang.String) value;
        } else if ("identifier".equals(fieldName)) {
            identifier = (java.lang.String) value;
        } else if ("url".equals(fieldName)) {
            url = (java.lang.String) value;
        } else if ("source".equals(fieldName)) {
            source = (org.intermine.model.InterMineObject) value;
        } else if ("subject".equals(fieldName)) {
            subject = (org.intermine.model.InterMineObject) value;
        } else if ("id".equals(fieldName)) {
            id = (java.lang.Integer) value;
        } else {
            if (!org.intermine.model.bio.CrossReference.class.equals(getClass())) {
                DynamicUtil.setFieldValue(this, fieldName, value);
                return;
            }
            throw new IllegalArgumentException("Unknown field " + fieldName);
        }
    }
    public Class<?> getFieldType(final String fieldName) {
        if ("dbxreftype".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("identifier".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("url".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("source".equals(fieldName)) {
            return org.intermine.model.bio.DataSource.class;
        }
        if ("subject".equals(fieldName)) {
            return org.intermine.model.bio.BioEntity.class;
        }
        if ("id".equals(fieldName)) {
            return java.lang.Integer.class;
        }
        if (!org.intermine.model.bio.CrossReference.class.equals(getClass())) {
            return TypeUtil.getFieldType(org.intermine.model.bio.CrossReference.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public StringConstructor getoBJECT() {
        if (!org.intermine.model.bio.CrossReferenceShadow.class.equals(getClass())) {
            return NotXmlRenderer.render(this);
        }
        StringConstructor sb = new StringConstructor();
        sb.append("$_^org.intermine.model.bio.CrossReference");
        if (dbxreftype != null) {
            sb.append("$_^adbxreftype$_^");
            String string = dbxreftype;
            while (string != null) {
                int delimPosition = string.indexOf("$_^");
                if (delimPosition == -1) {
                    sb.append(string);
                    string = null;
                } else {
                    sb.append(string.substring(0, delimPosition + 3));
                    sb.append("d");
                    string = string.substring(delimPosition + 3);
                }
            }
        }
        if (identifier != null) {
            sb.append("$_^aidentifier$_^");
            String string = identifier;
            while (string != null) {
                int delimPosition = string.indexOf("$_^");
                if (delimPosition == -1) {
                    sb.append(string);
                    string = null;
                } else {
                    sb.append(string.substring(0, delimPosition + 3));
                    sb.append("d");
                    string = string.substring(delimPosition + 3);
                }
            }
        }
        if (url != null) {
            sb.append("$_^aurl$_^");
            String string = url;
            while (string != null) {
                int delimPosition = string.indexOf("$_^");
                if (delimPosition == -1) {
                    sb.append(string);
                    string = null;
                } else {
                    sb.append(string.substring(0, delimPosition + 3));
                    sb.append("d");
                    string = string.substring(delimPosition + 3);
                }
            }
        }
        if (source != null) {
            sb.append("$_^rsource$_^").append(source.getId());
        }
        if (subject != null) {
            sb.append("$_^rsubject$_^").append(subject.getId());
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
        if (!org.intermine.model.bio.CrossReferenceShadow.class.equals(getClass())) {
            throw new IllegalStateException("Class " + getClass().getName() + " does not match code (org.intermine.model.bio.CrossReference)");
        }
        for (int i = 2; i < notXml.length;) {
            int startI = i;
            if ((i < notXml.length) && "adbxreftype".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                dbxreftype = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) && "aidentifier".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                identifier = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) && "aurl".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                url = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) &&"rsource".equals(notXml[i])) {
                i++;
                source = new ProxyReference(os, Integer.valueOf(notXml[i]), org.intermine.model.bio.DataSource.class);
                i++;
            };
            if ((i < notXml.length) &&"rsubject".equals(notXml[i])) {
                i++;
                subject = new ProxyReference(os, Integer.valueOf(notXml[i]), org.intermine.model.bio.BioEntity.class);
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
    }
    public void addCollectionElement(final String fieldName, final org.intermine.model.InterMineObject element) {
        {
            if (!org.intermine.model.bio.CrossReference.class.equals(getClass())) {
                TypeUtil.addCollectionElement(this, fieldName, element);
                return;
            }
            throw new IllegalArgumentException("Unknown collection " + fieldName);
        }
    }
    public Class<?> getElementType(final String fieldName) {
        if (!org.intermine.model.bio.CrossReference.class.equals(getClass())) {
            return TypeUtil.getElementType(org.intermine.model.bio.CrossReference.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
}
