package net.pardini.libvirt.domain;

import org.libvirt.Connect;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class LibVirtDomain {

    public static void main(String[] args) {
        Domain domain = Domain.builder().withType("kvm")
                .withDevices(Devices.builder().addController(Controller.builder().withModelAttr("model atribute").withType("pci").withModel().withName("wtf").end().build()).build())
                .build();
        System.out.println(domainToXMLStr(domain));

        try {
            Connect connect = new Connect("qemu:///system");
            for (String oneDomain : connect.listDefinedDomains()) {
                org.libvirt.Domain lookedUpDomain = connect.domainLookupByName(oneDomain);
                System.out.println(lookedUpDomain);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Domain.Builder<Void> domainBuilder() {
        return Domain.builder();
    }

    public static String domainToXMLStr(Domain domain) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Domain.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(domain, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("JAXB write failure with LibVirt Model: " + e.getMessage(), e);
        }
    }

    public static Domain xmlStrToDomain(String xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(Domain.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (Domain) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new RuntimeException("JAXB read failure with LibVirt Model: " + e.getMessage(), e);
        }
    }
}
