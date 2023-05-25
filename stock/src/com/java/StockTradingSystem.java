package com.java;

/**
 * @author yyq
 * @create 2023-05-23 8:52
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class StockTradingSystem {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/stock_trading?serverTimezone=UTC";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // 连接到数据库
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            while (true) {
                System.out.println("请选择操作:");
                System.out.println("1.股票管理");
                System.out.println("2.用户管理");
                System.out.println("3.交易功能");
                System.out.println("4.导出个人的历史交易记录");
                System.out.println("5.导出所有历史交易记录");
                System.out.println("6.退出");
                System.out.print("输入选项: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // 读取换行符

                switch (choice) {
                    case 1:
                        manageStocks(connection, scanner);
                        break;
                    case 2:
                        manageUsers(connection, scanner);
                        break;
                    case 3:
                        performTransaction(connection, scanner);
                        break;
                    case 4:
                            exportTransactionHistory(connection, scanner);
                            break;
                    case 5:
                        exportAllTransactionHistory(connection, scanner);
                        break;
                    case 6:
                        System.out.println("退出程序");
                        return;
                    default:
                        System.out.println("无效的选项");
                        break;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void manageStocks(Connection connection, Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("股票管理");
            System.out.println("1.添加股票");
            System.out.println("2.删除股票");
            System.out.println("3.更新股票信息");
            System.out.println("4.查询股票信息");
            System.out.println("5.返回上级菜单");
            System.out.print("输入选项: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 读取换行符

            switch (choice) {
                case 1:
                    addStock(connection, scanner);
                    break;
                case 2:
                    deleteStock(connection, scanner);
                    break;
                case 3:
                    updateStock(connection, scanner);
                    break;
                case 4:
                    queryStock(connection, scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("无效的选项");
                    break;
            }
        }
    }

    private static void addStock(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("添加股票");
        System.out.print("股票代码: ");
        String code = scanner.nextLine();
        System.out.print("股票名称: ");
        String name = scanner.nextLine();
        System.out.print("当前价格: ");
        double price = scanner.nextDouble();
        scanner.nextLine(); // 读取换行符
        System.out.print("股票数量: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // 读取换行符
        System.out.print("上市日期: ");
        String listingDate = scanner.nextLine();
        System.out.print("法人代表: ");
        String legalRepresentative = scanner.nextLine();
        System.out.print("所属行业: ");
        String industry = scanner.nextLine();

        // 执行插入语句
        String sql = "INSERT INTO stocks (code, name, price, quantity, listed_date, legal_representative, industry) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, code);
        statement.setString(2, name);
        statement.setDouble(3, price);
        statement.setInt(4, quantity);
        statement.setString(5, listingDate);
        statement.setString(6, legalRepresentative);
        statement.setString(7, industry);
        statement.executeUpdate();

        System.out.println("股票添加成功");
    }

    private static void deleteStock(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("删除股票");
        System.out.print("请输入要删除的股票代码: ");
        String code = scanner.nextLine();

        // 执行删除语句
        String sql = "DELETE FROM stocks WHERE code = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, code);
        int affectedRows = statement.executeUpdate();

        if (affectedRows > 0) {
            System.out.println("股票删除成功");
        } else {
            System.out.println("未找到匹配的股票");
        }
    }

    private static void updateStock(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("更新股票信息");
        System.out.print("请输入要更新的股票代码: ");
        String code = scanner.nextLine();

        // 检查股票是否存在
        String checkSql = "SELECT * FROM stocks WHERE code = ?";
        PreparedStatement checkStatement = connection.prepareStatement(checkSql);
        checkStatement.setString(1, code);
        ResultSet resultSet = checkStatement.executeQuery();

        if (!resultSet.next()) {
            System.out.println("未找到匹配的股票");
            return;
        }

        // 输入新的股票信息
        System.out.print("新的股票名称 (留空表示不修改): ");
        String newName = scanner.nextLine();
        System.out.print("新的当前价格 (留空表示不修改): ");
        String newPriceStr = scanner.nextLine();
        System.out.print("新的股票数量 (留空表示不修改): ");
        String newQuantityStr = scanner.nextLine();
        System.out.print("新的上市日期 (留空表示不修改): ");
        String newListingDate = scanner.nextLine();
        System.out.print("新的法人代表 (留空表示不修改): ");
        String newLegalRepresentative = scanner.nextLine();
        System.out.print("新的所属行业 (留空表示不修改): ");
        String newIndustry = scanner.nextLine();

        // 构造更新语句
        StringBuilder updateSqlBuilder = new StringBuilder("UPDATE stocks SET");
        boolean updateNeeded = false;

        if (!newName.isEmpty()) {
            updateSqlBuilder.append(" name = '").append(newName).append("',");
            updateNeeded = true;
        }

        if (!newPriceStr.isEmpty()) {
            double newPrice = Double.parseDouble(newPriceStr);
            updateSqlBuilder.append(" price = ").append(newPrice).append(",");
            updateNeeded = true;
        }

        if (!newQuantityStr.isEmpty()) {
            int newQuantity = Integer.parseInt(newQuantityStr);
            updateSqlBuilder.append(" quantity = ").append(newQuantity).append(",");
            updateNeeded = true;
        }

        if (!newListingDate.isEmpty()) {
            updateSqlBuilder.append(" listed_date = '").append(newListingDate).append("',");
            updateNeeded = true;
        }

        if (!newLegalRepresentative.isEmpty()) {
            updateSqlBuilder.append(" legal_representative = '").append(newLegalRepresentative).append("',");
            updateNeeded = true;
        }

        if (!newIndustry.isEmpty()) {
            updateSqlBuilder.append(" industry = '").append(newIndustry).append("',");
            updateNeeded = true;
        }

        if (!updateNeeded) {
            System.out.println("未输入任何要更新的信息");
            return;
        }

        // 执行更新语句
        String updateSql = updateSqlBuilder.substring(0, updateSqlBuilder.length() - 1) + " WHERE code = ?";
        PreparedStatement updateStatement = connection.prepareStatement(updateSql);
        updateStatement.setString(1, code);
        int affectedRows = updateStatement.executeUpdate();

        System.out.println("股票信息更新成功");
    }

    private static void queryStock(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("查询股票信息");
        System.out.print("请输入要查询的股票代码: ");
        String code = scanner.nextLine();

        // 执行查询语句
        String sql = "SELECT * FROM stocks WHERE code = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, code);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            String name = resultSet.getString("name");
            double price = resultSet.getDouble("price");
            int quantity = resultSet.getInt("quantity");
            String listingDate = resultSet.getString("listed_date");
            String legalRepresentative = resultSet.getString("legal_representative");
            String industry = resultSet.getString("industry");

            System.out.println("股票信息:");
            System.out.println("代码: " + code);
            System.out.println("名称: " + name);
            System.out.println("当前价格: " + price);
            System.out.println("数量: " + quantity);
            System.out.println("上市日期: " + listingDate);
            System.out.println("法人代表: " + legalRepresentative);
            System.out.println("所属行业: " + industry);
        } else {
            System.out.println("未找到匹配的股票");
        }
    }

    private static void manageUsers(Connection connection, Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("用户管理");
            System.out.println("1.添加用户");
            System.out.println("2.删除用户");
            System.out.println("3.更新用户信息");
            System.out.println("4.查询用户信息");
            System.out.println("5.返回上级菜单");
            System.out.print("输入选项: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 读取换行符

            switch (choice) {
                case 1:
                    addUser(connection, scanner);
                    break;
                case 2:
                    deleteUser(connection, scanner);
                    break;
                case 3:
                    updateUser(connection, scanner);
                    break;
                case 4:
                    queryUser(connection, scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("无效的选项");
                    break;
            }
        }
    }

    private static void addUser(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("添加用户");
        System.out.print("用户名: ");
        String username = scanner.nextLine();
        System.out.print("账户余额: ");
        double balance = scanner.nextDouble();
        scanner.nextLine(); // 读取换行符
        System.out.print("持有的股票代码: ");
        String stockCode = scanner.nextLine();
        System.out.print("持有的股票名称: ");
        String stockName = scanner.nextLine();
        System.out.print("持有的股票数量: ");
        int stockQuantity = scanner.nextInt();
        scanner.nextLine(); // 读取换行符

        // 执行插入语句
        String sql = "INSERT INTO users (username, balance, stock_code, stock_name, stock_quantity) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        statement.setDouble(2, balance);
        statement.setString(3, stockCode);
        statement.setString(4, stockName);
        statement.setInt(5, stockQuantity);
        statement.executeUpdate();

        System.out.println("用户添加成功");
    }

    private static void deleteUser(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("删除用户");
        System.out.print("请输入要删除的用户名: ");
        String username = scanner.nextLine();

        // 执行删除语句
        String sql = "DELETE FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        int affectedRows = statement.executeUpdate();

        if (affectedRows > 0) {
            System.out.println("用户删除成功");
        } else {
            System.out.println("未找到匹配的用户");
        }
    }

    private static void updateUser(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("更新用户信息");
        System.out.print("请输入要更新的用户名: ");
        String username = scanner.nextLine();

        // 检查用户是否存在
        String checkSql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement checkStatement = connection.prepareStatement(checkSql);
        checkStatement.setString(1, username);
        ResultSet resultSet = checkStatement.executeQuery();

        if (!resultSet.next()) {
            System.out.println("未找到匹配的用户");
            return;
        }

        // 输入新的用户信息
        System.out.print("新的账户余额 (留空表示不修改): ");
        String newBalanceStr = scanner.nextLine();
        System.out.print("新的持有的股票代码 (留空表示不修改): ");
        String newStockCode = scanner.nextLine();
        System.out.print("新的持有的股票名称 (留空表示不修改): ");
        String newStockName = scanner.nextLine();
        System.out.print("新的持有的股票数量 (留空表示不修改): ");
        String newStockQuantityStr = scanner.nextLine();

        // 构造更新语句
        StringBuilder updateSqlBuilder = new StringBuilder("UPDATE users SET");
        boolean updateNeeded = false;

        if (!newBalanceStr.isEmpty()) {
            double newBalance = Double.parseDouble(newBalanceStr);
            updateSqlBuilder.append(" balance = ").append(newBalance).append(",");
            updateNeeded = true;
        }

        if (!newStockCode.isEmpty()) {
            updateSqlBuilder.append(" stock_code = '").append(newStockCode).append("',");
            updateNeeded = true;
        }

        if (!newStockName.isEmpty()) {
            updateSqlBuilder.append(" stock_name = '").append(newStockName).append("',");
            updateNeeded = true;
        }

        if (!newStockQuantityStr.isEmpty()) {
            int newStockQuantity = Integer.parseInt(newStockQuantityStr);
            updateSqlBuilder.append(" stock_quantity = ").append(newStockQuantity).append(",");
            updateNeeded = true;
        }

        if (!updateNeeded) {
            System.out.println("未输入任何要更新的信息");
            return;
        }

        // 执行更新语句
        String updateSql = updateSqlBuilder.substring(0, updateSqlBuilder.length() - 1) + " WHERE username = ?";
        PreparedStatement updateStatement = connection.prepareStatement(updateSql);
        updateStatement.setString(1, username);
        int affectedRows = updateStatement.executeUpdate();

        System.out.println("用户信息更新成功");
    }

    private static void queryUser(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("查询用户信息");
        System.out.print("请输入要查询的用户名: ");
        String username = scanner.nextLine();

        // 执行查询语句
        String sql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            double balance = resultSet.getDouble("balance");
            String stockCode = resultSet.getString("stock_code");
            String stockName = resultSet.getString("stock_name");
            int stockQuantity = resultSet.getInt("stock_quantity");

            System.out.println("用户信息:");
            System.out.println("用户名: " + username);
            System.out.println("账户余额: " + balance);
            System.out.println("持有的股票代码: " + stockCode);
            System.out.println("持有的股票名称: " + stockName);
            System.out.println("持有的股票数量: " + stockQuantity);
        } else {
            System.out.println("未找到匹配的用户");
        }
    }

    private static void performTransaction(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("交易功能");
        System.out.print("请输入用户名: ");
        String username = scanner.nextLine();

        // 检查用户是否存在
        String checkUserSql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement checkUserStatement = connection.prepareStatement(checkUserSql);
        checkUserStatement.setString(1, username);
        ResultSet userResultSet = checkUserStatement.executeQuery();

        if (!userResultSet.next()) {
            System.out.println("未找到匹配的用户");
            return;
        }

        double balance = userResultSet.getDouble("balance");
        String stockCode = userResultSet.getString("stock_code");
        String stockName = userResultSet.getString("stock_name");
        int stockQuantity = userResultSet.getInt("stock_quantity");

        System.out.println("当前账户余额: " + balance);
        System.out.println("当前持有的股票代码: " + stockCode);
        System.out.println("当前持有的股票名称: " + stockName);
        System.out.println("当前持有的股票数量: " + stockQuantity);

        System.out.print("请选择交易类型 (1.购买 2.卖出): ");
        int transactionType = scanner.nextInt();
        scanner.nextLine(); // 读取换行符

        if (transactionType == 1) {
            buyStock(connection, scanner, username, balance, stockCode, stockQuantity);
        } else if (transactionType == 2) {
            sellStock(connection, scanner, username, balance, stockCode, stockQuantity);
        } else {
            System.out.println("无效的交易类型");
        }
    }

    private static void buyStock(Connection connection, Scanner scanner, String username, double balance, String stockCode, int stockQuantity) throws SQLException {
        System.out.println("购买股票");
        System.out.print("请输入要购买的股票代码: ");
        String code = scanner.nextLine();

        // 检查股票是否存在
        String checkStockSql = "SELECT * FROM stocks WHERE code = ?";
        PreparedStatement checkStockStatement = connection.prepareStatement(checkStockSql);
        checkStockStatement.setString(1, code);
        ResultSet stockResultSet = checkStockStatement.executeQuery();

        if (!stockResultSet.next()) {
            System.out.println("未找到匹配的股票");
            return;
        }

        String name = stockResultSet.getString("name");
        double price = stockResultSet.getDouble("price");
        int availableQuantity = stockResultSet.getInt("quantity");

        System.out.println("股票信息:");
        System.out.println("代码: " + code);
        System.out.println("名称: " + name);
        System.out.println("价格: " + price);
        System.out.println("可用数量: " + availableQuantity);

        System.out.print("请输入购买数量: ");
        int buyQuantity = scanner.nextInt();
        scanner.nextLine(); // 读取换行符

        if (buyQuantity <= 0) {
            System.out.println("购买数量必须大于0");
            return;
        }

        double totalCost = price * buyQuantity;

        if (totalCost > balance) {
            System.out.println("余额不足，无法购买股票");
            return;
        }

        if (buyQuantity > availableQuantity) {
            System.out.println("购买数量超过可用数量");
            return;
        }

        // 更新用户账户余额和持股信息
        double newBalance = balance - totalCost;
        int newStockQuantity = stockQuantity + buyQuantity;

        String updateUserSql = "UPDATE users SET balance = ?, stock_code = ?, stock_name = ?, stock_quantity = ? WHERE username = ?";
        PreparedStatement updateUserStatement = connection.prepareStatement(updateUserSql);
        updateUserStatement.setDouble(1, newBalance);
        updateUserStatement.setString(2, code);
        updateUserStatement.setString(3, name);
        updateUserStatement.setInt(4, newStockQuantity);
        updateUserStatement.setString(5, username);
        int updateUserRows = updateUserStatement.executeUpdate();

        // 更新股票数量
        int newAvailableQuantity = availableQuantity - buyQuantity;
        String updateStockSql = "UPDATE stocks SET quantity = ? WHERE code = ?";
        PreparedStatement updateStockStatement = connection.prepareStatement(updateStockSql);
        updateStockStatement.setInt(1, newAvailableQuantity);
        updateStockStatement.setString(2, code);
        int updateStockRows = updateStockStatement.executeUpdate();

        if (updateUserRows > 0 && updateStockRows > 0) {
            System.out.println("股票购买成功");
            System.out.println("购买数量: " + buyQuantity);
            System.out.println("总花费: " + totalCost);
            System.out.println("账户余额: " + newBalance);
            System.out.println("持有的股票代码: " + code);
            System.out.println("持有的股票名称: " + name);
            System.out.println("持有的股票数量: " + newStockQuantity);
        } else {
            System.out.println("股票购买失败");
        }
        // 在购买股票完成后，将交易记录插入到transaction_history表中
        String insertTransactionSql = "INSERT INTO transaction_history (username, stock_code, stock_name, quantity, price, transaction_type, transaction_time) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionSql);
        insertTransactionStatement.setString(1, username);
        insertTransactionStatement.setString(2, code);
        insertTransactionStatement.setString(3, name);
        insertTransactionStatement.setInt(4, buyQuantity);
        insertTransactionStatement.setDouble(5, price);
        insertTransactionStatement.setString(6, "购买");
        insertTransactionStatement.executeUpdate();
    }

    private static void sellStock(Connection connection, Scanner scanner, String username, double balance, String stockCode, int stockQuantity) throws SQLException {
        System.out.println("卖出股票");
        System.out.print("请输入要卖出的股票代码: ");
        String code = scanner.nextLine();

        if (!code.equals(stockCode)) {
            System.out.println("你当前持有的股票代码与输入的股票代码不符");
            return;
        }

        System.out.print("请输入卖出数量: ");
        int sellQuantity = scanner.nextInt();
        scanner.nextLine(); // 读取换行符

        if (sellQuantity <= 0) {
            System.out.println("卖出数量必须大于0");
            return;
        }

        if (sellQuantity > stockQuantity) {
            System.out.println("卖出数量超过持有数量");
            return;
        }

        // 获取股票价格
        String getStockSql = "SELECT * FROM stocks WHERE code = ?";
        PreparedStatement getStockStatement = connection.prepareStatement(getStockSql);
        getStockStatement.setString(1, code);
        ResultSet stockResultSet = getStockStatement.executeQuery();

        if (!stockResultSet.next()) {
            System.out.println("未找到匹配的股票");
            return;
        }

        double price = stockResultSet.getDouble("price");

        // 更新用户账户余额和持股信息
        double totalEarnings = price * sellQuantity;
        double newBalance = balance + totalEarnings;
        int newStockQuantity = stockQuantity - sellQuantity;

        String updateUserSql = "UPDATE users SET balance = ?, stock_quantity = ? WHERE username = ?";
        PreparedStatement updateUserStatement = connection.prepareStatement(updateUserSql);
        updateUserStatement.setDouble(1, newBalance);
        updateUserStatement.setInt(2, newStockQuantity);
        updateUserStatement.setString(3, username);
        int updateUserRows = updateUserStatement.executeUpdate();

        // 更新股票数量
        String updateStockSql = "UPDATE stocks SET quantity = quantity + ? WHERE code = ?";
        PreparedStatement updateStockStatement = connection.prepareStatement(updateStockSql);
        updateStockStatement.setInt(1, sellQuantity);
        updateStockStatement.setString(2, code);
        int updateStockRows = updateStockStatement.executeUpdate();

        if (updateUserRows > 0 && updateStockRows > 0) {
            System.out.println("股票卖出成功");
            System.out.println("卖出数量: " + sellQuantity);
            System.out.println("总收益: " + totalEarnings);
            System.out.println("账户余额: " + newBalance);
            System.out.println("持有的股票代码: " + code);
            System.out.println("持有的股票名称: " + stockCode);
            System.out.println("持有的股票数量: " + newStockQuantity);
        } else {
            System.out.println("股票卖出失败");
        }
        // 在卖出股票完成后，将交易记录插入到transaction_history表中
        String insertTransactionSql = "INSERT INTO transaction_history (username, stock_code, stock_name, quantity, price, transaction_type, transaction_time) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionSql);
        insertTransactionStatement.setString(1, username);
        insertTransactionStatement.setString(2, code);
        insertTransactionStatement.setString(3, stockCode);
        insertTransactionStatement.setInt(4, sellQuantity);
        insertTransactionStatement.setDouble(5, price);
        insertTransactionStatement.setString(6, "卖出");
        insertTransactionStatement.executeUpdate();
    }

    private static void exportTransactionHistory(Connection connection, Scanner scanner) throws SQLException, IOException {
        System.out.println("导出历史交易记录");
        System.out.print("请输入要导出记录的用户名: ");
        String username = scanner.nextLine();

        // 执行查询语句
        String sql = "SELECT * FROM transaction_history WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        ResultSet resultSet = statement.executeQuery();

        if (!resultSet.next()) {
            System.out.println("未找到匹配的交易记录");
            return;
        }

        // 构造文件
        String filename = username + "_transaction_history.txt";

        // 创建文件并写入记录
        File file = new File(filename);
        FileWriter writer = new FileWriter(file);

        do {
            int transactionId = resultSet.getInt("id");
            String stockCode = resultSet.getString("stock_code");
            String stockName = resultSet.getString("stock_name");
            int quantity = resultSet.getInt("quantity");
            double price = resultSet.getDouble("price");
            String transactionType = resultSet.getString("transaction_type");
            String transactionTime = resultSet.getString("transaction_time");

            String transactionRecord = "交易ID: " + transactionId + "\n" +
                    "股票代码: " + stockCode + "\n" +
                    "股票名称: " + stockName + "\n" +
                    "交易数量: " + quantity + "\n" +
                    "交易价格: " + price + "\n" +
                    "交易类型: " + transactionType + "\n" +
                    "交易时间: " + transactionTime + "\n\n";

            writer.write(transactionRecord);
        } while (resultSet.next());

        writer.close();

        System.out.println("交易记录已成功导出到文件: " + filename);
    }
    private static void exportAllTransactionHistory(Connection connection, Scanner scanner) throws SQLException, IOException {
        System.out.println("导出历史交易记录");

        // 执行查询语句，检索所有交易记录
        String sql = "SELECT * FROM transaction_history";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        if (!resultSet.next()) {
            System.out.println("未找到交易记录");
            return;
        }

        // 构造文件名
        String filename = "transaction_history.txt";

        // 创建文件并写入记录
        File file = new File(filename);
        FileWriter writer = new FileWriter(file);

        do {
            int transactionId = resultSet.getInt("id");
            String username = resultSet.getString("username");
            String stockCode = resultSet.getString("stock_code");
            String stockName = resultSet.getString("stock_name");
            int quantity = resultSet.getInt("quantity");
            double price = resultSet.getDouble("price");
            String transactionType = resultSet.getString("transaction_type");
            String transactionTime = resultSet.getString("transaction_time");

            String transactionRecord = "交易ID: " + transactionId + "\n" +
                    "用户名: " + username + "\n" +
                    "股票代码: " + stockCode + "\n" +
                    "股票名称: " + stockName + "\n" +
                    "交易数量: " + quantity + "\n" +
                    "交易价格: " + price + "\n" +
                    "交易类型: " + transactionType + "\n" +
                    "交易时间: " + transactionTime + "\n\n";

            writer.write(transactionRecord);
        } while (resultSet.next());

        writer.close();

        System.out.println("交易记录已成功导出到文件: " + filename);
    }


}




