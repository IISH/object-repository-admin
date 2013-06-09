package org.objectrepository.iso9660

import org.objectrepository.iso9660.impl.Iso9660FileEntry
import org.objectrepository.iso9660.impl.Iso9660FileSystem

/**
 * Created with IntelliJ IDEA.
 * User: lwo
 * Date: 6/8/13
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
class Test {

    public static void main(String[] args) throws IOException {

        final File file = new File(args[0]);
        final Iso9660FileSystem iso9660FileSystem = new Iso9660FileSystem(file, true);
        for (Iso9660FileEntry fileEntry : iso9660FileSystem) {
            System.out.println("getName" + fileEntry.getName());
            System.out.println("getPath" + fileEntry.getPath());
            System.out.println("getSize" + fileEntry.getSize());
            System.out.println("getLastModifiedTime" + fileEntry.getLastModifiedTime());
            System.out.println("isDirectory" + fileEntry.isDirectory());
            if (fileEntry.isDirectory()) {
            } else
                write(fileEntry.getName(), iso9660FileSystem.getInputStream(fileEntry));
        }

    }

    private static void write(String name, InputStream inputStream) throws IOException {

        int read;
        byte[] bytes = new byte[1024];

        FileOutputStream outputStream = new FileOutputStream("/tmp/" + name);
        while ((read = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }
        outputStream.close();
        inputStream.close();
    }

}
