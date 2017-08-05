/**
 * Created by sandulmv on 28.07.17.
 */

public class Document {

  // URL address
  private String documentName;

  public Document(String documentName) {
    if (documentName == null) {
      throw new IllegalArgumentException("Document name should not be null");
    }
    this.documentName = documentName;
  }

  @Override
  public String toString() {
    return documentName;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Document)) {
      return false;
    }

    if (other == this) {
      return true;
    }

    Document otherDocument = (Document) other;
    return documentName.equals(otherDocument.documentName);
  }

  @Override
  public int hashCode() {
    return documentName.hashCode();
  }
}
