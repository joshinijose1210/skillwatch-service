import { SSMClient, GetParameterCommand, PutParameterCommand } from "@aws-sdk/client-ssm";

const ssm = new SSMClient({ region: "us-west-2" });

async function getReleaseTag() {
  const command = new GetParameterCommand({ Name: "/release/tag" });
  const response = await ssm.send(command);
  return parseInt(response.Parameter?.Value || "0", 10);
}

async function setReleaseTag(tag: number) {
  const command = new PutParameterCommand({
    Name: "/release/tag",
    Value: tag.toString(),
    Overwrite: true,
  });
  await ssm.send(command);
}

async function main() {
  const oldTag = await getReleaseTag();
  const newTag = oldTag + 1;
  await setReleaseTag(newTag);
  console.log(`New release tag: ${newTag}`);
}

main().catch(console.error);
