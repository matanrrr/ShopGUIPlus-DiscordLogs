package mr.matanr.shopGUIPlusDiscordLogs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Webhook {
    private final String url;
    private String username;
    private String avatarUrl;
    private List<EmbedObject> embeds = new ArrayList<>();

    public Webhook(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void send() throws IOException {
        if (this.url == null)
            return;

        StringBuilder json = new StringBuilder();
        json.append("{");

        if (this.username != null) {
            json.append("\"username\": \"").append(quote(this.username)).append("\",");
        }

        if (this.avatarUrl != null) {
            json.append("\"avatar_url\": \"").append(quote(this.avatarUrl)).append("\",");
        }

        if (!this.embeds.isEmpty()) {
            json.append("\"embeds\": [");
            for (int i = 0; i < this.embeds.size(); i++) {
                EmbedObject embed = this.embeds.get(i);
                json.append("{");

                if (embed.title != null) json.append("\"title\": \"").append(quote(embed.title)).append("\",");
                if (embed.description != null) json.append("\"description\": \"").append(quote(embed.description)).append("\",");

                if (embed.color != null) {
                    int rgb = embed.color.getRed();
                    rgb = (rgb << 8) + embed.color.getGreen();
                    rgb = (rgb << 8) + embed.color.getBlue();
                    json.append("\"color\": ").append(rgb).append(",");
                }

                // Author
                if (embed.author != null) {
                    json.append("\"author\": {");
                    json.append("\"name\": \"").append(quote(embed.author.name)).append("\",");
                    json.append("\"url\": \"").append(quote(embed.author.url)).append("\",");
                    json.append("\"icon_url\": \"").append(quote(embed.author.iconUrl)).append("\"");
                    json.append("},");
                }

                // Footer
                if (embed.footer != null) {
                    json.append("\"footer\": {");
                    json.append("\"text\": \"").append(quote(embed.footer.text)).append("\",");
                    json.append("\"icon_url\": \"").append(quote(embed.footer.iconUrl)).append("\"");
                    json.append("},");
                }

                // Thumbnail
                if (embed.thumbnail != null) {
                    json.append("\"thumbnail\": {");
                    json.append("\"url\": \"").append(quote(embed.thumbnail.url)).append("\"");
                    json.append("},");
                }

                // Image
                if (embed.image != null) {
                    json.append("\"image\": {");
                    json.append("\"url\": \"").append(quote(embed.image.url)).append("\"");
                    json.append("},");
                }

                if (!embed.fields.isEmpty()) {
                    json.append("\"fields\": [");
                    for (int j = 0; j < embed.fields.size(); j++) {
                        Field field = embed.fields.get(j);
                        json.append("{");
                        json.append("\"name\": \"").append(quote(field.name)).append("\",");
                        json.append("\"value\": \"").append(quote(field.value)).append("\",");
                        json.append("\"inline\": ").append(field.inline);
                        json.append("}").append(j < embed.fields.size() - 1 ? "," : "");
                    }
                    json.append("]");
                }
                json.append("}").append(i < this.embeds.size() - 1 ? "," : "");
            }
            json.append("]");
        }
        json.append("}");

        URL url = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(json.toString().getBytes());
            stream.flush();
        }

        connection.getInputStream().close();
        connection.disconnect();
    }

    private String quote(String string) {
        return string == null ? "" : string.replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private java.awt.Color color;
        private Author author;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private List<Field> fields = new ArrayList<>();

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setColor(java.awt.Color color) {
            this.color = color;
        }

        public void setAuthor(String name, String url, String iconUrl) {
            this.author = new Author(name, url, iconUrl);
        }

        public void setFooter(String text, String iconUrl) {
            this.footer = new Footer(text, iconUrl);
        }

        public void setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
        }

        public void setImage(String url) {
            this.image = new Image(url);
        }

        public void addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
        }
    }

    private static class Field {
        String name, value;
        boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }

    private static class Author {
        String name, url, iconUrl;
        public Author(String name, String url, String iconUrl) {
            this.name = name; this.url = url; this.iconUrl = iconUrl;
        }
    }

    private static class Footer {
        String text, iconUrl;
        public Footer(String text, String iconUrl) {
            this.text = text; this.iconUrl = iconUrl;
        }
    }

    private static class Thumbnail {
        String url;
        public Thumbnail(String url) { this.url = url; }
    }

    private static class Image {
        String url;
        public Image(String url) { this.url = url; }
    }
}