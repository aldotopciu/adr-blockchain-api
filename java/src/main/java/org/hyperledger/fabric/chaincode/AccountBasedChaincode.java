package org.hyperledger.fabric.chaincode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
//import io.netty.handler.ssl.OpenSsl;
import org.hyperledger.fabric.chaincode.Models.*;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

public class AccountBasedChaincode extends ChaincodeBase {
    private class ChaincodeResponse {
        public String message;
        public String code;
        public boolean OK;

        public ChaincodeResponse(String message, String code, boolean OK) {
            this.code = code;
            this.message = message;
            this.OK = OK;
        }
    }

    private String responseError(String errorMessage, String code) {
        try {
            return (new ObjectMapper()).writeValueAsString(new ChaincodeResponse(errorMessage, code, false));
        } catch (Throwable e) {
            return "{\"code\":'" + code + "', \"message\":'" + e.getMessage() + " AND " + errorMessage + "', \"OK\":" + false + "}";
        }
    }

    private String responseSuccess(String successMessage) {
        try {
            return (new ObjectMapper()).writeValueAsString(new ChaincodeResponse(successMessage, "", true));
        } catch (Throwable e) {
            return "{\"message\":'" + e.getMessage() + " BUT " + successMessage + " (NO COMMIT)', \"OK\":" + false + "}";
        }
    }

    private String responseSuccessObject(String object) {
        return "{\"message\":" + object + ", \"OK\":" + true + "}";
    }

    private boolean checkString(String str) {
        if (str.trim().length() <= 0 || str == null)
            return false;
        return true;
    }

    @Override
    public Response init(ChaincodeStub stub) {
        return newSuccessResponse(responseSuccess("OKAY"));
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        String func = stub.getFunction();
        List<String> params = stub.getParameters();
        if (func.equals("createWallet"))
            return createWallet(stub, params);
        else if (func.equals("getWallet"))
            return getWallet(stub, params);
        else if (func.equals("transfer"))
            return transfer(stub, params);
        else if (func.equals("multipleTransfer"))
            return transfer(stub, params);
        else if (func.equals("getWalletsWithTokenAmountGreaterThan"))
            return getWalletsWithTokenAmountGreaterThan(stub, params);
        else if (func.equals("createWork"))
            return  createWork(stub , params);
        /*else if (func.equals("modifyWork"))
            return  modifyWork(stub , params);*/
        else if (func.equals("getWork"))
            return  getWork(stub , params);
        return newErrorResponse(responseError("Unsupported method", ""));
    }

    // query the blockchain with a rich query and append the results one by one in an array
    private String query(String queryString, ChaincodeStub stub) {
        String result = "[";
        QueryResultsIterator<KeyValue> rows = stub.getQueryResult(queryString);
        while (rows.iterator().hasNext()) {
            String v = rows.iterator().next().getStringValue();
            if(v != null && v.trim().length() > 0) {
                result = result.concat(v);
                if (rows.iterator().hasNext())
                    result = result.concat(",");
            }
        }
        return result.concat("]");
    }

    private Response createWallet(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 2", ""));
        String walletId = args.get(0);
        String tokenAmount = args.get(1);
        if (!checkString(walletId) || !checkString(tokenAmount))
            return newErrorResponse(responseError("Invalid argument(s)", ""));

        BigDecimal tokenAmountBigDecimal;
        try {
            tokenAmountBigDecimal = new BigDecimal(tokenAmount);
            if(tokenAmountBigDecimal.doubleValue() < 0.0)
                return newErrorResponse(responseError("Invalid token amount", ""));
        } catch (NumberFormatException e) {
            return newErrorResponse(responseError("Parsing error", ""));
        }

        Wallet wallet = new Wallet(walletId, tokenAmountBigDecimal);
        try {
            if(checkString(stub.getStringState(walletId)))
                return newErrorResponse(responseError("Existent wallet", ""));
            stub.putState(walletId, (new ObjectMapper()).writeValueAsBytes(wallet));
            return newSuccessResponse(responseSuccess("Wallet created"));
        } catch (Throwable e) {
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    // accept a token amount and call the query() function to find all wallets with token amount > that amount above
    private Response getWalletsWithTokenAmountGreaterThan(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 1", ""));
        String tokenAmountStr = args.get(0);
        if (!checkString(tokenAmountStr))
            return newErrorResponse(responseError("Invalid argument", ""));

        try {
            BigDecimal tokenAmount = new BigDecimal(tokenAmountStr);
            String queryStr = "{ \"selector\": { \"tokenAmount\": { \"$gt\": " + tokenAmount + " } } }";
            String queryResult = query(queryStr, stub);
            return newSuccessResponse((new ObjectMapper()).writeValueAsBytes(responseSuccessObject(queryResult)));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    private Response getWallet(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 1", ""));
        String walletId = args.get(0);
        if (!checkString(walletId))
            return newErrorResponse(responseError("Invalid argument", ""));
        try {
            String walletString = stub.getStringState(walletId);
            if(!checkString(walletString))
                return newErrorResponse(responseError("Nonexistent wallet", ""));
            return newSuccessResponse((new ObjectMapper()).writeValueAsBytes(responseSuccessObject(walletString)));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    // get a work based on the isrc code
    private Response getWork(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 1", ""));
        String workISRC = args.get(0);
        if (!checkString(workISRC))
            return newErrorResponse(responseError("Invalid argument", ""));
        try {
            String workString = stub.getStringState(workISRC);
            if(!checkString(workString))
                return newErrorResponse(responseError("Non existent work", ""));
            return newSuccessResponse((new ObjectMapper()).writeValueAsBytes(responseSuccessObject(workString)));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    // transfer token amount from a wallet to another
    private Response transfer(ChaincodeStub stub, List<String> args) {
        if (args.size() != 3)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 3", ""));
        String fromWalletId = args.get(0);
        String toWalletId = args.get(1);
        String tokenAmount = args.get(2);
        if (!checkString(fromWalletId) || !checkString(toWalletId) || !checkString(tokenAmount))
            return newErrorResponse(responseError("Invalid argument(s)", ""));
        if(fromWalletId.equals(toWalletId))
            return newErrorResponse(responseError("From-wallet is same as to-wallet", ""));

        BigDecimal tokenAmountBigDecimal;
        try {
            tokenAmountBigDecimal = new BigDecimal(tokenAmount);
            if(tokenAmountBigDecimal.doubleValue() < 0.0)
                return newErrorResponse(responseError("Invalid token amount", ""));
        } catch (NumberFormatException e) {
            return newErrorResponse(responseError("Parse error", ""));
        }

        try {
            String fromWalletString = stub.getStringState(fromWalletId);
            if(!checkString(fromWalletString))
                return newErrorResponse(responseError("Non existent from-wallet", ""));
            String toWalletString = stub.getStringState(toWalletId);
            if(!checkString(toWalletString))
                return newErrorResponse(responseError("Non existent to-wallet", ""));

            ObjectMapper objectMapper = new ObjectMapper();
            Wallet fromWallet = objectMapper.readValue(fromWalletString, Wallet.class);
            Wallet toWallet = objectMapper.readValue(toWalletString, Wallet.class);

            if(fromWallet.getTokenAmount().compareTo(tokenAmountBigDecimal)<0)
                return newErrorResponse(responseError("Token amount not enough", ""));

            fromWallet.setTokenAmount(fromWallet.getTokenAmount().subtract(tokenAmountBigDecimal));
            toWallet.setTokenAmount(toWallet.getTokenAmount().add(tokenAmountBigDecimal));
            stub.putState(fromWalletId, objectMapper.writeValueAsBytes(fromWallet));
            stub.putState(toWalletId, objectMapper.writeValueAsBytes(toWallet));

            return newSuccessResponse(responseSuccess("Transferred"));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    // Multiple transfers from a wallet to others
    private Response multipleTransfer(ChaincodeStub stub, List<String> args) {
        if (args.size() < 4)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting at least 3", ""));
        String fromWalletId = args.get(0);

        if (!checkString(fromWalletId))
            return newErrorResponse(responseError("Invalid argument(s)", ""));
        int length = args.size();
        for(int i = 1; i<length; i++) {
            String toWalletId = args.get(i);
            String tokenAmount = args.get(++i);
            if (fromWalletId.equals(toWalletId))
                return newErrorResponse(responseError("From-wallet is same as to-wallet", ""));

            BigDecimal tokenAmountBigDecimal;
            try {
                tokenAmountBigDecimal = new BigDecimal(tokenAmount);
                if (tokenAmountBigDecimal.doubleValue() < 0.0)
                    return newErrorResponse(responseError("Invalid token amount", ""));
            } catch (NumberFormatException e) {
                return newErrorResponse(responseError("Parse error", ""));
            }

            try {
                String fromWalletString = stub.getStringState(fromWalletId);
                if (!checkString(fromWalletString))
                    return newErrorResponse(responseError("Non existent from-wallet", ""));
                String toWalletString = stub.getStringState(toWalletId);
                if (!checkString(toWalletString))
                    return newErrorResponse(responseError("Non existent to-wallet", ""));

                ObjectMapper objectMapper = new ObjectMapper();
                Wallet fromWallet = objectMapper.readValue(fromWalletString, Wallet.class);
                Wallet toWallet = objectMapper.readValue(toWalletString, Wallet.class);

                if (fromWallet.getTokenAmount().compareTo(tokenAmountBigDecimal) < 0)
                    return newErrorResponse(responseError("Token amount not enough", ""));

                fromWallet.setTokenAmount(fromWallet.getTokenAmount().subtract(tokenAmountBigDecimal));
                toWallet.setTokenAmount(toWallet.getTokenAmount().add(tokenAmountBigDecimal));
                stub.putState(fromWalletId, objectMapper.writeValueAsBytes(fromWallet));
                stub.putState(toWalletId, objectMapper.writeValueAsBytes(toWallet));

            } catch (Throwable e) {
                return newErrorResponse(responseError(e.getMessage(), ""));
            }
        }
        return newSuccessResponse(responseSuccess("Transferred"));
    }

    // create a work
    private Response createWork(ChaincodeStub stub, List<String> args) {
        if (args.size() < 7)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting at least 5", ""));
        String ISRC = args.get(0).toString();
        String ISWC = args.get(1).toString();
        String title = args.get(2).toString();
        String managerId = args.get(3).toString();
        if (!checkString(ISRC) || !checkString(ISWC) || !checkString(title) || !checkString(managerId))
            return newErrorResponse(responseError("Invalid argument(s)", ""));
        int length = args.size();
        List<RightHolder> rightHolders = new ArrayList<RightHolder>();
        List<NonRightHolder> nonRightHolders = new ArrayList<NonRightHolder>();
        Work work;
        int i = 4;
        for(;i<length; i++){
            if(args.get(i).equals("NonRightHolders")) break;
            if (!checkString(args.get(i)) || !checkString(args.get(i+1)) || !checkString(args.get(i+2)))
                return newErrorResponse(responseError("Invalid argument(s)", ""));
            Double percentage = 0.0;
            try{
                percentage = Double.parseDouble(args.get(i+2));
            }catch(NumberFormatException e){}
            RightHolder rightHolder = new RightHolder(args.get(i), Attribute.valueOf(args.get(i+1)),percentage);
            rightHolders.add(rightHolder);
            i+=2;
        }
        for(;i<length; i++){
            if (!checkString(args.get(i)))
                return newErrorResponse(responseError("Invalid argument(s)", ""));
            NonRightHolder nonRightHolder = new NonRightHolder(args.get(i));
            nonRightHolders.add(nonRightHolder);
        }
        if(nonRightHolders.isEmpty()){
            work = new Work(ISRC, ISWC, managerId, title, rightHolders);
        }
        else {
            work = new Work(ISRC, ISWC, managerId, title, rightHolders, nonRightHolders);
        }
        try {
            if(checkString(stub.getStringState(ISRC)))
                return newErrorResponse(responseError("Existent work", ""));
            stub.putState(ISRC, (new ObjectMapper()).writeValueAsBytes(work));
            return newSuccessResponse(responseSuccess("Work created"));
        } catch (Throwable e) {
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    // modify a work still to implement

    public static void main(String[] args) {

        //System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new AccountBasedChaincode().start(args);
    }
}
