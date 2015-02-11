package org.patientview.api.util;
/*
 * Copyright (c) 2014 Kevin Hunter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


        import java.io.File;
        import java.io.IOException;
        import java.util.Iterator;

        import javax.imageio.ImageIO;
        import javax.imageio.ImageReader;
        import javax.imageio.metadata.IIOMetadata;
        import javax.imageio.stream.ImageInputStream;

        import org.w3c.dom.NamedNodeMap;
        import org.w3c.dom.Node;

/**
 * Dump the ImageIO metadata associated with one or more images.
 *
 * @author Kevin Hunter
 *
 */
public class DumpImageMetaData
{
    private static String getFileExtension(File file)
    {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        return fileName.substring(lastDot + 1);
    }

    private static void indent(int level)
    {
        for (int i = 0; i < level; i++)
        {
            System.out.print("    ");
        }
    }

    private static void displayAttributes(NamedNodeMap attributes)
    {
        if (attributes != null)
        {
            int count = attributes.getLength();
            for (int i = 0; i < count; i++)
            {
                Node attribute = attributes.item(i);

                System.out.print(" ");
                System.out.print(attribute.getNodeName());
                System.out.print("='");
                System.out.print(attribute.getNodeValue());
                System.out.print("'");
            }
        }
    }

    private static void displayMetadataNode(Node node, int level)
    {
        indent(level);
        System.out.print("<");
        System.out.print(node.getNodeName());

        NamedNodeMap attributes = node.getAttributes();
        displayAttributes(attributes);

        Node child = node.getFirstChild();
        if (child == null)
        {
            String value = node.getNodeValue();
            if (value == null || value.length() == 0)
            {
                System.out.println("/>");
            }
            else
            {
                System.out.print(">");
                System.out.print(value);
                System.out.print("<");
                System.out.print(node.getNodeName());
                System.out.println(">");
            }
            return;
        }

        System.out.println(">");
        while (child != null)
        {
            displayMetadataNode(child, level + 1);
            child = child.getNextSibling();
        }

        indent(level);
        System.out.print("</");
        System.out.print(node.getNodeName());
        System.out.println(">");
    }

    public static void dumpMetadata(IIOMetadata metadata)
    {
        String[] names = metadata.getMetadataFormatNames();
        int length = names.length;
        for (int i = 0; i < length; i++)
        {
            indent(2);
            System.out.println("Format name: " + names[i]);
            displayMetadataNode(metadata.getAsTree(names[i]), 3);
        }
    }

    private static void processFileWithReader(File file, ImageReader reader) throws IOException
    {
        ImageInputStream stream = null;

        try
        {
            stream = ImageIO.createImageInputStream(file);

            reader.setInput(stream, true);

            IIOMetadata metadata = reader.getImageMetadata(0);

            indent(1);
            System.out.println("Image metadata");
            dumpMetadata(metadata);

            metadata = reader.getStreamMetadata();
            if (metadata != null)
            {
                indent(1);
                System.out.println("Stream metadata");
                dumpMetadata(metadata);
            }

        }
        finally
        {
            if (stream != null)
            {
                stream.close();
            }
        }
    }

    public static void processFile(File file) throws IOException
    {
        System.out.println("\nProcessing " + file.getName() + ":\n");

        String extension = getFileExtension(file);

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(extension);

        while (readers.hasNext())
        {
            ImageReader reader = readers.next();

            System.out.println("Reader: " + reader.getClass().getName());

            processFileWithReader(file, reader);
        }
    }

    private static void processDirectory(File directory) throws IOException
    {
        System.out.println("Processing all files in " + directory.getAbsolutePath());

        File[] contents = directory.listFiles();
        for (File file : contents)
        {
            if (file.isFile())
            {
                processFile(file);
            }
        }
    }

    public static void main(String[] args)
    {
        try
        {
            for (int i = 0; i < args.length; i++)
            {
                File fileOrDirectory = new File(args[i]);

                if (fileOrDirectory.isFile())
                {
                    processFile(fileOrDirectory);
                }
                else
                {
                    processDirectory(fileOrDirectory);
                }
            }

            System.out.println("\nDone");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}