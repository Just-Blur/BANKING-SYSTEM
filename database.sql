USE BANKING_SYSTEM;
GO

CREATE TABLE Customers (
    CustomerID INT IDENTITY(1001,1) PRIMARY KEY,
    FirstName VARCHAR(100) NOT NULL,
    Address_ VARCHAR(200) NOT NULL,
    PhoneNumber VARCHAR(20) NOT NULL
);
GO

CREATE TABLE Accounts (
    AccountNumber   VARCHAR(30)   PRIMARY KEY,
    CustomerID      INT           NOT NULL,
    AccountType     VARCHAR(30)   NOT NULL,
    Balance         DECIMAL(18,2) NOT NULL DEFAULT 0,
    Branch          VARCHAR(100)  NOT NULL,
    OpeningDate     DATE          NOT NULL DEFAULT GETDATE(),
    EmployerName    VARCHAR(100)  NULL,
    EmployerAddress VARCHAR(200)  NULL,

    FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),

    CHECK (Balance >= 0),
    CHECK (AccountType IN ('Investment', 'Savings', 'Cheque'))
);
GO

CREATE TABLE Transactions (
    TransactionID INT IDENTITY(1,1) PRIMARY KEY,
    AccountNumber VARCHAR(30) NOT NULL,
    TransactionType VARCHAR(30) NOT NULL,
    Amount DECIMAL(18,2) NOT NULL,
    TransactionDate DATETIME NOT NULL DEFAULT GETDATE(),
    BalanceAfter DECIMAL(18,2) NOT NULL,

    FOREIGN KEY (AccountNumber) REFERENCES Accounts(AccountNumber),

    CHECK (Amount > 0),
    CHECK (TransactionType IN ('Deposit', 'Withdrawal', 'Transfer'))
);
GO