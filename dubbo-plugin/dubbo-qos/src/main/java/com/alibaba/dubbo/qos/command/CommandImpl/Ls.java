package com.alibaba.dubbo.qos.command.CommandImpl;

import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ConsumerModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.qos.command.BaseCommand;
import com.alibaba.dubbo.qos.command.CommandContext;
import com.alibaba.dubbo.qos.command.annotation.Cmd;
import com.alibaba.dubbo.qos.textui.TTable;
import com.alibaba.dubbo.registry.support.ConsumerInvokerWrapper;
import com.alibaba.dubbo.registry.support.ProviderConsumerRegTable;
import com.alibaba.dubbo.registry.support.ProviderInvokerWrapper;

import java.util.Collection;
import java.util.Set;

/**
 * @author qinliujie
 * @date 2017/11/22
 */
@Cmd(name = "ls", summary = "ls service", example = {
        "ls"
})
public class Ls implements BaseCommand {
    @Override
    public String execute(CommandContext commandContext, String[] args) {
        StringBuilder result = new StringBuilder();
        result.append(listProvier());
        result.append(listConsumer());

        return result.toString();
    }

    public String listProvier() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("As Provider side:\n");
        Collection<ProviderModel> ProviderModelList = ApplicationModel.allProviderModels();

        TTable tTable = new TTable(new TTable.ColumnDefine[]{
                new TTable.ColumnDefine(TTable.Align.MIDDLE),
                new TTable.ColumnDefine(TTable.Align.MIDDLE)
        });

        //Header
        tTable.addRow("Provider Service Name", "PUB");

        //Content
        for (ProviderModel providerModel : ProviderModelList) {
            tTable.addRow(providerModel.getServiceName(), isReg(providerModel.getServiceName()) ? "Y" : "N");
        }
        stringBuilder.append(tTable.rendering());

        return stringBuilder.toString();
    }

    public String listConsumer() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("As Consumer side:\n");
        Collection<ConsumerModel> consumerModelList = ApplicationModel.allConsumerModels();

        TTable tTable = new TTable(new TTable.ColumnDefine[]{
                new TTable.ColumnDefine(TTable.Align.MIDDLE),
                new TTable.ColumnDefine(TTable.Align.MIDDLE)
        });

        //Header
        tTable.addRow("Consumer Service Name", "NUM");

        //Content
        //TODO to calculate consumerAddressNum
        for (ConsumerModel consumerModel : consumerModelList) {
            tTable.addRow(consumerModel.getServiceName(), getConsumerAddressNum(consumerModel.getServiceName()));
        }

        stringBuilder.append(tTable.rendering());

        return stringBuilder.toString();
    }

    private boolean isReg(String serviceUniqueName) {
        Set<ProviderInvokerWrapper> providerInvokerWrapperSet = ProviderConsumerRegTable.getProviderInvoker(serviceUniqueName);
        for (ProviderInvokerWrapper providerInvokerWrapper : providerInvokerWrapperSet) {
            if (providerInvokerWrapper.isReg()) {
                return true;
            }
        }

        return false;
    }

    private int getConsumerAddressNum(String serviceUniqueName) {
        int count = 0;
        Set<ConsumerInvokerWrapper> providerInvokerWrapperSet = ProviderConsumerRegTable.getConsumerInvoker(serviceUniqueName);
        for (ConsumerInvokerWrapper consumerInvokerWrapper : providerInvokerWrapperSet) {
            //TODO not thread safe,fixme
            int addNum = 0;
            if (consumerInvokerWrapper.getRegistryDirectory().getUrlInvokerMap() != null) {
                addNum = consumerInvokerWrapper.getRegistryDirectory().getUrlInvokerMap().size();
            }
            count += addNum;
        }
        return count;
    }
}
