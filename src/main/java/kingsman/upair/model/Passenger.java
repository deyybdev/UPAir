package kingsman.upair.model;

/**
 * Model class representing a Passenger's personal information
 * Follows OOP principles with encapsulation
 */
public class Passenger {
    private String username;
    private String firstName;
    private String lastName;
    private String cellphoneNumber;
    private String province;
    private String city;
    private String barangay;
    private String idType;
    private String idNumber;
    
    // Default constructor
    public Passenger() {
    }
    
    // Parameterized constructor
    public Passenger(String username, String firstName, String lastName, 
                    String cellphoneNumber, String province, String city, 
                    String barangay, String idType, String idNumber) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cellphoneNumber = cellphoneNumber;
        this.province = province;
        this.city = city;
        this.barangay = barangay;
        this.idType = idType;
        this.idNumber = idNumber;
    }
    
    // Getters and Setters (Encapsulation)
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getCellphoneNumber() {
        return cellphoneNumber;
    }
    
    public void setCellphoneNumber(String cellphoneNumber) {
        this.cellphoneNumber = cellphoneNumber;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getBarangay() {
        return barangay;
    }
    
    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }
    
    public String getIdType() {
        return idType;
    }
    
    public void setIdType(String idType) {
        this.idType = idType;
    }
    
    public String getIdNumber() {
        return idNumber;
    }
    
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    
    /**
     * Validates if all required fields are filled
     * @return true if all fields are valid, false otherwise
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               cellphoneNumber != null && !cellphoneNumber.trim().isEmpty() &&
               province != null && !province.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               barangay != null && !barangay.trim().isEmpty() &&
               idType != null && !idType.trim().isEmpty() && !idType.equals("Select ID Type") &&
               idNumber != null && !idNumber.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "Passenger{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", cellphoneNumber='" + cellphoneNumber + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", barangay='" + barangay + '\'' +
                ", idType='" + idType + '\'' +
                ", idNumber='" + idNumber + '\'' +
                '}';
    }
}

