/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wrmsr.tokamak.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.wrmsr.tokamak.util.json.Json;
import com.wrmsr.tokamak.util.io.CrLfByteReader;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public final class Docker
{
    private Docker()
    {
    }

    public static SocketAddress socketAddress(String socketName)
            throws IOException
    {
        int colon = socketName.lastIndexOf(':');
        int slashOrBackslash = Math.max(socketName.lastIndexOf('/'), socketName.lastIndexOf('\\'));

        if (socketName.startsWith("@")) {
            return AFUNIXSocketAddress.inAbstractNamespace(socketName.substring(1));
        }
        else if (colon > 0 && slashOrBackslash < colon && !socketName.startsWith("/")) {
            String hostname = socketName.substring(0, colon);
            int port = Integer.parseInt(socketName.substring(colon + 1));
            return new InetSocketAddress(hostname, port);
        }
        else {
            return new AFUNIXSocketAddress(new File(socketName));
        }
    }

    public static Socket connectSocket(SocketAddress socketAddress)
            throws IOException
    {
        if (socketAddress instanceof AFUNIXSocketAddress) {
            return AFUNIXSocket.connectTo((AFUNIXSocketAddress) socketAddress);
        }
        else {
            Socket socket = new Socket();
            socket.connect(socketAddress);
            return socket;
        }
    }

    //  echo -e 'GET /containers/json HTTP/1.1\r\nHost: v1.40\r\nAccept: */*\r\n' | socat - UNIX-CONNECT:/var/run/docker.sock

    public static final class Container
    {
        public static final class Port
        {
            private final int privatePort;
            private final int publicPort;
            private final String type;

            @JsonCreator
            public Port(
                    @JsonProperty("PrivatePort") int privatePort,
                    @JsonProperty("PublicPort") int publicPort,
                    @JsonProperty("Type") String type)
            {
                this.privatePort = privatePort;
                this.publicPort = publicPort;
                this.type = type;
            }

            @Override
            public String toString()
            {
                return "Port{" +
                        "privatePort=" + privatePort +
                        ", publicPort=" + publicPort +
                        ", type='" + type + '\'' +
                        '}';
            }

            @JsonProperty("PrivatePort")
            public int getPrivatePort()
            {
                return privatePort;
            }

            @JsonProperty("PublicPort")
            public int getPublicPort()
            {
                return publicPort;
            }

            @JsonProperty("Type")
            public String getType()
            {
                return type;
            }
        }

        private final String id;
        private final Set<String> names;
        private final String image;
        private final String imageId;
        private final List<Port> ports;

        @JsonCreator
        public Container(
                @JsonProperty("Id") String id,
                @JsonProperty("Names") Set<String> names,
                @JsonProperty("Image") String image,
                @JsonProperty("ImageID") String imageId,
                @JsonProperty("Ports") List<Port> ports)
        {
            this.id = id;
            this.names = names;
            this.image = image;
            this.imageId = imageId;
            this.ports = ports;
        }

        @Override
        public String toString()
        {
            return "Container{" +
                    "id='" + id + '\'' +
                    ", names=" + names +
                    ", image='" + image + '\'' +
                    ", imageId='" + imageId + '\'' +
                    ", ports=" + ports +
                    '}';
        }

        @JsonProperty("Id")
        public String getId()
        {
            return id;
        }

        @JsonProperty("Names")
        public Set<String> getNames()
        {
            return names;
        }

        @JsonProperty("Image")
        public String getImage()
        {
            return image;
        }

        @JsonProperty("ImageID")
        public String getImageId()
        {
            return imageId;
        }

        @JsonProperty("Ports")
        public List<Port> getPorts()
        {
            return ports;
        }
    }

    public static final String DEFAULT_ADDRESS = "/var/run/docker.sock";

    public static List<Container> queryDockerContainers(String address)
            throws IOException
    {
        byte[] buf;
        try (Socket sock = connectSocket(socketAddress(address))) {
            OutputStream os = sock.getOutputStream();
            os.write(Joiner.on("\r\n").join(
                    "GET /containers/json HTTP/1.1",
                    "Host: v1.40",
                    "Accept: */*",
                    ""
            ).getBytes(Charsets.UTF_8));
            os.close();
            buf = ByteStreams.toByteArray(sock.getInputStream());
        }

        CrLfByteReader reader = new CrLfByteReader(new ByteArrayInputStream(buf));
        String code = reader.nextLineAscii();
        checkState(code.equals("HTTP/1.1 200 OK"));

        while (true) {
            byte[] line = reader.nextLine();
            if (line.length == 0) {
                break;
            }
        }

        String body = reader.restUtf8();

        return Json.readValue(body, new TypeReference<List<Container>>() {});
    }

    public static List<Container> queryDockerContainers()
            throws IOException
    {
        return queryDockerContainers(DEFAULT_ADDRESS);
    }
}
