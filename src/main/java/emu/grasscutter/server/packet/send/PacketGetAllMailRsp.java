package emu.grasscutter.server.packet.send;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.Mail;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetAllMailRspOuterClass.GetAllMailRsp;
import emu.grasscutter.net.proto.ItemParamOuterClass;
import emu.grasscutter.net.proto.MailDataOuterClass;
import emu.grasscutter.net.proto.MailDataOuterClass.MailData;
import emu.grasscutter.net.proto.MailItemOuterClass;
import emu.grasscutter.net.proto.MailTextContentOuterClass.MailTextContent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PacketGetAllMailRsp extends GenshinPacket {

    public PacketGetAllMailRsp(GenshinPlayer player, boolean isGiftMail) {
        super(PacketOpcodes.GetAllMailRsp);

        if (isGiftMail) {
            // TODO: Gift Mail
            // Make sure to send the stupid empty packet
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] rsp = decoder.decode("IAE=");
            try {
                GetAllMailRsp var = GetAllMailRsp.parseFrom(rsp);
                this.setData(var.toBuilder().build());
            } catch (Exception e) {
            }

        } else {
            if (player.getAllMail().size() != 0) { // Make sure the player has mail
                GetAllMailRsp.Builder proto = GetAllMailRsp.newBuilder();
                List<MailData> mailDataList = new ArrayList<MailData>();

                for (Mail message : player.getAllMail()) {

                    if(message.stateValue == 1) { // Make sure it isn't a gift
                        Grasscutter.getLogger().info("a");
                        if (message.expireTime > (int) Instant.now().getEpochSecond()) { // Make sure the message isn't expired (The game won't show expired mail, but I don't want to send unnecessary information).
                            Grasscutter.getLogger().info("b");
                            if(mailDataList.size() <= 1000) { // Make sure that there isn't over 1000 messages in the mailbox. (idk what will happen if there is but the game probably won't like it.)
                                Grasscutter.getLogger().info("c");
                                MailTextContent.Builder mailTextContent = MailTextContent.newBuilder();
                                mailTextContent.setTitle(message.mailContent.title);
                                mailTextContent.setContent(message.mailContent.content);
                                mailTextContent.setSender(message.mailContent.sender);

                                List<MailItemOuterClass.MailItem> mailItems = new ArrayList<>();

                                for (Mail.MailItem item : message.itemList) {
                                    MailItemOuterClass.MailItem.Builder mailItem = MailItemOuterClass.MailItem.newBuilder();
                                    ItemParamOuterClass.ItemParam.Builder itemParam = ItemParamOuterClass.ItemParam.newBuilder();
                                    itemParam.setItemId(item.itemId);
                                    itemParam.setCount(item.itemCount);
                                    mailItem.setItemParam(itemParam.build());

                                    mailItems.add(mailItem.build());
                                }

                                MailDataOuterClass.MailData.Builder mailData = MailDataOuterClass.MailData.newBuilder();
                                mailData.setMailId(message._id);
                                mailData.setMailTextContent(mailTextContent.build());
                                mailData.addAllItemList(mailItems);
                                mailData.setSendTime((int) message.sendTime);
                                mailData.setExpireTime((int) message.expireTime);
                                mailData.setImportance(message.importance);
                                mailData.setIsRead(message.isRead);
                                mailData.setIsAttachmentGot(message.isAttachmentGot);
                                mailData.setStateValue(1);

                                mailDataList.add(mailData.build());
                            }
                        }
                    }
                }

                proto.addAllMailList(mailDataList);
                proto.setIsTruncated(mailDataList.size() <= 1000 ? false : true); // When enabled this will send a notification to the user telling them their inbox is full and they should delete old messages when opening the mailbox.

                this.setData(proto.build());
            } else {
                // Make sure to send the stupid empty packet
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] rsp = decoder.decode("IAE=");
                try {
                    GetAllMailRsp var = GetAllMailRsp.parseFrom(rsp);
                    this.setData(var.toBuilder().build());
                } catch (Exception e) {}
            }
        }
    }
}
