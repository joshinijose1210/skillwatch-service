package scalereal.core.kpi

import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.IBlockElement
import com.itextpdf.layout.element.ILeafElement
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import jakarta.inject.Singleton
import scalereal.core.models.domain.KPIData
import scalereal.core.models.domain.KPIGroupKey

@Singleton
class PdfGeneratorService {
    fun generateKpiPdf(kpis: List<KPIData>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(outputStream)
        val document = Document(PdfDocument(pdfWriter))

        document.add(
            Paragraph("KPI List")
                .setBold()
                .setFontSize(16f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f),
        )
        document.add(Paragraph("\n"))

        val groupedKPIs =
            kpis
                .flatMap { kpi ->
                    (kpi.kpiDepartmentTeamDesignations ?: emptyList()).map { dept ->
                        Pair(dept, kpi)
                    }
                }.groupBy { triple ->
                    KPIGroupKey(
                        triple.first.departmentName ?: "",
                        triple.first.teamName ?: "",
                        triple.first.designationNames.firstOrNull() ?: "",
                    )
                }

        val line = SolidLine(0.5f).apply { color = DeviceRgb(211, 211, 211) }

        fun addGroupInfo(
            title: String,
            value: String,
        ) {
            document.add(
                Paragraph()
                    .add(Text("$title: ").setBold())
                    .add(Text(value)),
            )
        }

        groupedKPIs.forEach { (groupKey, kpiTriples) ->
            addGroupInfo("Department", groupKey.department)
            addGroupInfo("Team", groupKey.team)
            addGroupInfo("Designation", groupKey.designation)
            document.add(Paragraph("\n"))
            document.add(LineSeparator(line))

            kpiTriples
                .groupBy { it.second.kraName ?: "" }
                .forEach { (kraName, kraKPIs) ->
                    document.add(Paragraph("\n"))
                    document.add(Paragraph("KRA - $kraName").setBold())

                    kraKPIs.forEach { (_, kpi) ->
                        document.add(Paragraph("\n"))
                        document.add(
                            Paragraph()
                                .add(Text("KPI ID: ").setBold())
                                .add(Text(kpi.kpiId))
                                .add(Text(" (Status: "))
                                .add(Text(if (kpi.status) "Active" else "Inactive"))
                                .add(Text(")")),
                        )
                        document.add(
                            Paragraph().add(Text("Title: ").setBold()).add(Text(kpi.title)),
                        )

                        document.add(Paragraph("Description:").setBold().setMargin(0f))

                        val elements = HtmlConverter.convertToElements(kpi.description)
                        elements.forEach { element ->
                            when (element) {
                                is Paragraph -> {
                                    element.setMargin(0f)
                                    document.add(element)
                                }
                                is ILeafElement -> {
                                    document.add(Paragraph().add(element).setMargin(0f))
                                }
                                is IBlockElement -> {
                                    document.add(element)
                                }
                                else -> {
                                    document.add(Paragraph(element.toString()).setMargin(0f))
                                }
                            }
                        }
                        document.add(Paragraph("\n"))
                        document.add(LineSeparator(line))
                    }
                }
        }

        document.close()
        return outputStream.toByteArray()
    }
}
