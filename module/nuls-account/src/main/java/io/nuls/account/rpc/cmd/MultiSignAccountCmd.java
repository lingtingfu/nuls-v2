package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.dto.MultiSignTransactionResultDto;
import io.nuls.account.service.AliasService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: EdwardChan
 * @description:
 * @date: Dec.20th 2018
 */
@Component
public class MultiSignAccountCmd extends BaseCmd {

    @Autowired
    private MultiSignAccountService multiSignAccountService;

    @Autowired
    private AliasService aliasService;

    /**
     * 创建多签账户
     * <p>
     * create a multi sign account
     *
     * @param params [chainId,pubKeys,minSigns]
     * @return
     */
    @CmdAnnotation(cmd = "ac_createMultiSigAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create a multi sign account")
    public Response createMultiSigAccount(Map params) {
        Map<String, Object> map = new HashMap<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object pubKeysObj = params == null ? null : params.get(RpcParameterNameConstant.PUB_KEYS);
            Object minSignsObj = params == null ? null : params.get(RpcParameterNameConstant.MIN_SIGNS);
            if (params == null || chainIdObj == null || pubKeysObj == null || minSignsObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            int minSigns = (int) minSignsObj;
            List<String> pubKeysList = (List<String>) pubKeysObj;
            //create the account
            MultiSigAccount multiSigAccount = multiSignAccountService.createMultiSigAccount(chainId, pubKeysList, minSigns);
            if (multiSigAccount == null) { //create failed
                throw new NulsRuntimeException(AccountErrorCode.FAILED);
            }
            List<Map<String,String>> pubKeys = new ArrayList<>();
            for (byte[] pubKeyBytes : multiSigAccount.getPubKeyList()) {
                Map<String,String> tmpMap = new HashMap<>();
                tmpMap.put("pubKey", RPCUtil.encode(pubKeyBytes));
                tmpMap.put("address",AddressTool.getStringAddressByBytes(AddressTool.getAddress(pubKeyBytes,chainId)));
                pubKeys.add(tmpMap);
            }
            map.put("address", multiSigAccount.getAddress().getBase58());
            map.put("minSigns", minSigns);
            map.put("pubKeys", pubKeys);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        return success(map);
    }

    /**
     * 导入多签账户
     * <p>
     * import multi sign account
     *
     * @param params [chainId,address,pubKeys,minSigns]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importMultiSigAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "inport a multi sign account")
    public Response importMultiSigAccount(Map params) {
        Map<String, Object> map = new HashMap<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object pubKeysObj = params == null ? null : params.get(RpcParameterNameConstant.PUB_KEYS);
            Object minSignsObj = params == null ? null : params.get(RpcParameterNameConstant.MIN_SIGNS);
            if (params == null || chainIdObj == null || addressObj == null || pubKeysObj == null || minSignsObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            List<String> pubKeys = (List<String>) pubKeysObj;
            int minSigns = (int) minSignsObj;
            //create the account
            MultiSigAccount multiSigAccount = multiSignAccountService.importMultiSigAccount(chainId, address, pubKeys, minSigns);
            if (multiSigAccount == null) { //create failed
                throw new NulsRuntimeException(AccountErrorCode.FAILED);
            }
            map.put("address", multiSigAccount.getAddress().getBase58());
            map.put("minSigns", minSigns);
            map.put("pubKeys", pubKeys);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        return success(map);
    }

    /**
     * 移出多签账户
     * <p>
     * import multi sign account
     *
     * @param params [chainId,address]
     * @return
     */
    @CmdAnnotation(cmd = "ac_removeMultiSigAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "remove the multi sign account")
    public Response removeMultiSigAccount(Map params) {
        Map<String, Object> map = new HashMap<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            //create the account
            boolean result = multiSignAccountService.removeMultiSigAccount(chainId, address);
            map.put("value", result);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        return success(map);
    }

    /**
     * set the alias of multi sign account
     *
     * @param params
     * @return txhash
     */
    @CmdAnnotation(cmd = "ac_setMultiSigAlias", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set the alias of multi sign account")
    public Object setMultiAlias(Map params) {
        Map<String, String> map = new HashMap<>();
        int chainId;
        String address, password, alias, signAddress, txHash = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        Object signAddressObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_ADDREESS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null || aliasObj == null
                    || signAddressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            password = (String) passwordObj;
            alias = (String) aliasObj;
            signAddress = (String) signAddressObj;


            if (!AddressTool.validAddress(chainId, signAddress) || !AddressTool.validAddress(chainId, address)) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            if (StringUtils.isBlank(alias)) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            if (!FormatValidUtils.validAlias(alias)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
            }
            if (!aliasService.isAliasUsable(chainId, alias)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_EXIST);
            }
            MultiSignTransactionResultDto multiSignTransactionResultDto = multiSignAccountService.setMultiAlias(chainId, address, password, alias, signAddress);
            if (multiSignTransactionResultDto.isBroadcasted()) {
                map.put("txHash", multiSignTransactionResultDto.getTransaction().getHash().toHex());
            } else {
                map.put("tx", RPCUtil.encode(multiSignTransactionResultDto.getTransaction().serialize()));
            }
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    /**
     * Search for multi-signature accounts by address
     *
     * @param params
     * @return txhash
     */
    @CmdAnnotation(cmd = "ac_getMultiSigAccount", version = 1.0, description = "Search for multi-signature accounts by address")
    public Object getMultiSigAccount(Map params) {
        int chainId;
        String address;
        MultiSigAccount multiSigAccount;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            if (!AddressTool.validAddress(chainId, address)) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(chainId, address);
            String data = null == multiSigAccount ? null : RPCUtil.encode(multiSigAccount.serialize());
            Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            map.put("value", data);
            return success(map);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

}
