//package com.xapc.client.render;
//
//import com.geckolib.renderer.base.BoneSnapshots;
//import com.xapc.item.ItemRegistry;
//import com.xapc.weapons.shootgun4;
//import net.minecraft.world.item.ItemDisplayContext;
//
//import com.geckolib.renderer.GeoItemRenderer;
//import com.geckolib.renderer.base.GeoRenderState;
//import com.geckolib.renderer.base.RenderPassInfo;
//import com.geckolib.constant.DataTickets;
//
//// ДОБАВЛЯЕМ ИМПОРТ ИНТЕРФЕЙСА СНАПШОТОВ ИЗ GECKOLIB
//
//public class ShootgunRenderer extends GeoItemRenderer<shootgun4> {
//    public ShootgunRenderer() {
//        super(ItemRegistry.SHOOTGUN4);
//    }
//
//    @Override
//    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
//        super.adjustModelBonesForRender(renderPassInfo, snapshots);
//
//        // 1. Извлекаем перспективу рендера через RenderPassInfo
//        ItemDisplayContext perspective = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, null);
//
//        // 2. Проверяем, держит ли игрок оружие от первого лица
//        boolean shouldShowArms = (perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
//                perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
//
//        // 3. Работаем СТРОГО через snapshots (так как сама кость иммутабельна)
//        snapshots.get("right_hand").ifPresent(snapshot -> {
//            if (shouldShowArms) {
//                snapshot.setScaleX(1.0F);
//                snapshot.setScaleY(1.0F);
//                snapshot.setScaleZ(1.0F);
//            } else {
//                snapshot.setScaleX(0.0F);
//                snapshot.setScaleY(0.0F);
//                snapshot.setScaleZ(0.0F);
//            }
//        });
//
//        // 4. ЛЕВАЯ РУКА: Точно так же меняем масштаб
//        snapshots.get("left_hand").ifPresent(snapshot -> {
//            if (shouldShowArms) {
//                snapshot.setScaleX(1.0F);
//                snapshot.setScaleY(1.0F);
//                snapshot.setScaleZ(1.0F);
//            } else {
//                snapshot.setScaleX(0.0F);
//                snapshot.setScaleY(0.0F);
//                snapshot.setScaleZ(0.0F);
//            }
//        });
//    }
//}