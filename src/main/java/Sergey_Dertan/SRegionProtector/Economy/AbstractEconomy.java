package Sergey_Dertan.SRegionProtector.Economy;

import io.gomint.entity.EntityPlayer;

public interface AbstractEconomy {

    long getMoney(EntityPlayer player);

    default void addMoney(EntityPlayer player, long amount) {
        this.addMoney(player.getName(), amount);
    }

    default void reduceMoney(EntityPlayer player, long amount) {
        this.reduceMoney(player.getName(), amount);
    }

    void reduceMoney(String player, long amount);

    void addMoney(String player, long amount);

}
