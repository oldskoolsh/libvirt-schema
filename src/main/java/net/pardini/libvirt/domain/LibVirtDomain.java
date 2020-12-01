package net.pardini.libvirt.domain;

import org.libvirt.Connect;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class LibVirtDomain {

    public static void main(String[] args) {
        Domain domain = Domain.builder().withType("kvm")
                .withDevices(Devices.builder().addController(Controller.builder().withModelAttr("model atribute").withType("pci").withModel().withName("wtf").end().build()).build())
                .build();
        System.out.println(domainToXMLStr(domain));

        try {
            Connect connect = new Connect("qemu:///system");
            org.libvirt.Domain lookedUpDomain = connect.domainLookupByName("smthlonger2.some.example.com");
            System.out.println(lookedUpDomain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException("JAXB failure with LibVirt Model: " + e.getMessage(), e);
        }
    }
}
