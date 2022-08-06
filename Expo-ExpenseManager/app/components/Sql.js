import * as SQLite from 'expo-sqlite';
import { categoryIcons } from '../config/Icons';

export const createTableQueries = {
    user: `
        CREATE TABLE IF NOT EXISTS
        User
        (Name TEXT PRIMARY KEY, Password TEXT);
    `,
    category: `
        CREATE TABLE IF NOT EXISTS
        Category 
        (Name TEXT PRIMARY KEY, Icon TEXT);
    `,
    account: `
        CREATE TABLE IF NOT EXISTS
        Account 
        (Name TEXT PRIMARY KEY, InitialAmount REAL);
    `,
    method: `
        CREATE TABLE IF NOT EXISTS
        Method 
        (
            Name TEXT PRIMARY KEY, Account TEXT,
            FOREIGN KEY (Account) REFERENCES Account (Name)
        );
    `,
    expense: `
        CREATE TABLE IF NOT EXISTS
        Expense 
        (
            Id INTEGER PRIMARY KEY AUTOINCREMENT,
            Day INTEGER, Month TEXT, Year INTEGER,
            OtherInfo TEXT, Files BLOB, IsIncome BOOL,
            Title TEXT, Amount REAL, Method TEXT, Category TEXT,
            FOREIGN KEY (Method) REFERENCES Method (Name),
            FOREIGN KEY (Category) REFERENCES Category (Name)
        );
    `,
}


const db = SQLite.openDatabase('ExpenseManager');

export var executeSql = async (
    sql, callbackSuccess = () => {}, 
    callbackError = (tx, err) => {console.log(`ErrorCallback ${tx}:${err}`)}
) => {
    try {
        db.transaction( (tx) => {
            tx.executeSql(
                sql,
                [],
                callbackSuccess,
                callbackError
            );
        });
    } catch (error) {
        console.warn(`Error in executing ${sql}: ${error}`)
    }
}

const initSqlQueries = async () => {
    await executeSql('PRAGMA foreign_keys = ON;');
    await executeSql(createTableQueries.user);
    await executeSql(createTableQueries.category);
    await executeSql(createTableQueries.account);
    await executeSql(createTableQueries.method);
    await executeSql(createTableQueries.expense);

    for (var category in categoryIcons) {
        await executeSql(
            `INSERT OR IGNORE INTO Category (Name, Icon) VALUES (
                '${category}', '${categoryIcons[category]}'
            );`
        );
    }
}

initSqlQueries();

export const Months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
]

/*  
    DONE
    1. Add delete option for categories/accounts/methods
    2. Add transfer action
    3. Confirm Expense without blob
    4. Add logout
    6. Change account to have initial amount
    7. Add credit/debit mode in expenses
    8. Edit Expense
    12. Fix the order of expense list
    13. Delete Expense
    14. Change safeview padding according to device
    15. Preload categories & Add icons

    IN PROGRESS
    16. Add balances
    5. Add charts
    9. Add search in expenses
    
    TODO
    10. Save/Load files/images
    17. Export to csv file
    11. Separate entries per user
    
*/