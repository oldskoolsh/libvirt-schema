package net.pardini.libvirt.domain;

import com.sun.jna.NativeLibrary;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibVirtDomain {

    public static void main(String[] args) {
        Domain hmm = Domain.builder().withMetadata(
                Metadata.builder().withLibosinfo().withOs().withId("os").end().end().build()
        ).build();

        Domain cpu = Domain.builder().build();

        Domain domain = Domain.builder().withType("kvm")
                .withDevices(Devices.builder().addController(Controller.builder().withModelAttr("model atribute").withType("pci").withModel().withName("wtf").end().build()).build())
                .build();
        System.out.println(domainToXMLStr(domain));

        try {
            showLibvirtInfoForConnection("qemu:///system");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void showLibvirtInfoForConnection(String uri) throws LibvirtException {
        Connect connect = safeConnect(uri);
        System.out.println("Connected to host " + connect.getHostName() + " via URI " + connect.getURI());
        System.out.println("Connected to version " + connect.getVersion() + " via lib version " + connect.getLibVersion());
        for (String oneDomain : connect.listDefinedDomains()) {
            org.libvirt.Domain lookedUpDomain = connect.domainLookupByName(oneDomain);
            System.out.println(lookedUpDomain);
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

    public static Connect safeConnect(String uri) {
        try {
            final String[] libNames = new String[]{"virt", "virt-qemu"};
            final String location = "/usr/local/Cellar/libvirt"; // magic for macos/brew
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + location + "/*/lib/libvirt.dylib");
            final List<String> foundLibs = new ArrayList<>();
            Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (pathMatcher.matches(path)) {
                        foundLibs.add(path.getParent().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            foundLibs.forEach(foundLib -> Arrays.stream(libNames).forEach(libName -> NativeLibrary.addSearchPath(libName, foundLib)));
        } catch (Exception ignored) {
        }

        try {
            return new Connect(uri);
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }

    }
}
