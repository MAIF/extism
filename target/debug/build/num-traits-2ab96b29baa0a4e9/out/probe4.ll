; ModuleID = 'probe4.44adcfaa9954dba4-cgu.0'
source_filename = "probe4.44adcfaa9954dba4-cgu.0"
target datalayout = "e-m:o-i64:64-i128:128-n32:64-S128"
target triple = "arm64-apple-macosx11.0.0"

@alloc_52849884032f8a57c30767a7da0a73fd = private unnamed_addr constant <{ [84 x i8] }> <{ [84 x i8] c"/private/tmp/rust-20231120-5297-163e829/rustc-1.74.0-src/library/core/src/num/mod.rs" }>, align 1
@alloc_9d73935bd1198707daa41eb95b04e8b6 = private unnamed_addr constant <{ ptr, [16 x i8] }> <{ ptr @alloc_52849884032f8a57c30767a7da0a73fd, [16 x i8] c"T\00\00\00\00\00\00\00v\04\00\00\05\00\00\00" }>, align 8
@str.0 = internal constant [25 x i8] c"attempt to divide by zero"

; probe4::probe
; Function Attrs: uwtable
define void @_ZN6probe45probe17hcff2b2054e1469daE() unnamed_addr #0 {
start:
  %0 = call i1 @llvm.expect.i1(i1 false, i1 false)
  br i1 %0, label %panic.i, label %"_ZN4core3num21_$LT$impl$u20$u32$GT$10div_euclid17he45184c217efc367E.exit"

panic.i:                                          ; preds = %start
; call core::panicking::panic
  call void @_ZN4core9panicking5panic17h236079fccf304236E(ptr align 1 @str.0, i64 25, ptr align 8 @alloc_9d73935bd1198707daa41eb95b04e8b6) #3
  unreachable

"_ZN4core3num21_$LT$impl$u20$u32$GT$10div_euclid17he45184c217efc367E.exit": ; preds = %start
  ret void
}

; Function Attrs: nocallback nofree nosync nounwind willreturn memory(none)
declare i1 @llvm.expect.i1(i1, i1) #1

; core::panicking::panic
; Function Attrs: cold noinline noreturn uwtable
declare void @_ZN4core9panicking5panic17h236079fccf304236E(ptr align 1, i64, ptr align 8) unnamed_addr #2

attributes #0 = { uwtable "frame-pointer"="non-leaf" "target-cpu"="apple-m1" }
attributes #1 = { nocallback nofree nosync nounwind willreturn memory(none) }
attributes #2 = { cold noinline noreturn uwtable "frame-pointer"="non-leaf" "target-cpu"="apple-m1" }
attributes #3 = { noreturn }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 8, !"PIC Level", i32 2}
!1 = !{!"rustc version 1.74.0 (79e9716c9 2023-11-13) (Homebrew)"}
