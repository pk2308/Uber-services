package Uber.context;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Hazelcast custom serialization
 *
 * @author kent
 * @param <T>
 */
public class ObjectStreamSerializer<T> implements StreamSerializer<T> {

    @Override
    public int getTypeId() {
        return 10;
    }

    @Override
    public void write(ObjectDataOutput objectDataOutput, T object)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(bos)) {
            encoder.writeObject(object);
        }
        objectDataOutput.write(bos.toByteArray());
    }

    @Override
    public T read(ObjectDataInput objectDataInput) throws IOException {
        InputStream inputStream = (InputStream) objectDataInput;
        XMLDecoder decoder = new XMLDecoder(inputStream);
        return (T) decoder.readObject();
    }

    @Override
    public void destroy() {
    }

}
