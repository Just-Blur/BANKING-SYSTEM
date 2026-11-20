import java.time.LocalDate;

public class ChequeAccount extends Account {
    private String employerName;
    private String employerAddress;

    public ChequeAccount(String accountNumber, double balance, String branch, LocalDate openingDate, Customer customer, String employerName, String employerAddress) {
        super(accountNumber, balance, branch, openingDate, "Cheque", customer);

        if (balance < 0) {
            throw new IllegalArgumentException(
                "Initial balance cannot be negative."
            );
        }

        if (employerName == null || employerName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Employer name is required for a cheque account."
            );
        }

        if (employerAddress == null || employerAddress.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Employer address is required for a cheque account."
            );
        }

        this.employerName = employerName.trim();
        this.employerAddress = employerAddress.trim();
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }
}